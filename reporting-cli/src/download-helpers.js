/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import puppeteer from 'puppeteer';
import fs from 'fs';
import { FORMAT, REPORT_TYPE, SELECTOR, AUTH, URL_SOURCE } from './constants.js';
import { exit } from "process";
import ora from 'ora';

const spinner = ora();

export async function downloadReport(url, format, width, height, filename, authType, username, password, tenant) {
  spinner.start('Connecting to url ' + url);
  try {
    const browser = await puppeteer.launch({
      headless: true,
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-gpu',
        '--no-zygote',
        '--font-render-hinting=none',
        '--enable-features=NetworkService',
        '--ignore-certificate-errors',
      ],
      executablePath: process.env.CHROMIUM_PATH,
      ignoreHTTPSErrors: true,
      env: {
        TZ: process.env.TZ || 'UTC',
      },
    });

    const page = await browser.newPage();
    const overridePage = await browser.newPage();
    page.setDefaultNavigationTimeout(0);
    page.setDefaultTimeout(300000);
    overridePage.setDefaultNavigationTimeout(0);
    overridePage.setDefaultTimeout(300000);

    // auth 
    if (authType !== undefined && authType !== AUTH.NONE && username !== undefined && password !== undefined) {
      if (authType === AUTH.BASIC_AUTH) {
        await basicAuthentication(page, overridePage, url, username, password, tenant);
      }
      else if (authType === AUTH.SAML_AUTH) {
        await samlAuthentication(page, url, username, password, tenant);
      }
      else if (authType === AUTH.COGNITO_AUTH) {
        await cognitoAuthentication(page, overridePage, url, username, password, tenant);
      }
      spinner.info('Credentials are verified');
    }
    // no auth
    else {
      await page.goto(url, { waitUntil: 'networkidle0' });
    }

    spinner.info('Connected to url ' + url);
    spinner.start('Loading page');
    await page.setViewport({
      width: width,
      height: height,
    });

    const reportSource = getReportSourceFromURL(url);

    // if its an OpenSearch report, remove extra elements.
    if (reportSource !== 'Other' && reportSource !== 'Saved search') {
      await page.evaluate(
        (reportSource, REPORT_TYPE) => {
          // remove buttons.
          document
            .querySelectorAll("[class^='euiButton']")
            .forEach((e) => e.remove());
          // remove top navBar.
          document
            .querySelectorAll("[class^='euiHeader']")
            .forEach((e) => e.remove());
          // remove visualization editor.
          if (reportSource === REPORT_TYPE.VISUALIZATION) {
            document
              .querySelector('[data-test-subj="splitPanelResizer"]')
              ?.remove();
            document.querySelector('.visEditor__collapsibleSidebar')?.remove();
          }
          document.body.style.paddingTop = '0px';
        },
        reportSource,
        REPORT_TYPE
      );
    }

    // force wait for any resize to load after the above DOM modification.
    await new Promise(resolve => setTimeout(resolve, 1000));

    switch (reportSource) {
      case REPORT_TYPE.DASHBOARD:
        await page.waitForSelector(SELECTOR.DASHBOARD, {
          visible: true,
        });
        break;
      case REPORT_TYPE.VISUALIZATION:
        await page.waitForSelector(SELECTOR.VISUALIZATION, {
          visible: true,
        });
        break;
      case REPORT_TYPE.NOTEBOOK:
        await page.waitForSelector(SELECTOR.NOTEBOOK, {
          visible: true,
        });
        break;
      case REPORT_TYPE.DISCOVER:
        await page.waitForSelector(SELECTOR.DISCOVER, {
          visible: true,
        });
        break;
      default:
        break;
    }

    await waitForDynamicContent(page);
    let buffer;
    spinner.text = `Downloading Report...`;

    // create pdf, png or csv accordingly
    if (format === FORMAT.PDF) {
      const scrollHeight = await page.evaluate(
        () => document.documentElement.scrollHeight
      );

      buffer = await page.pdf({
        margin: undefined,
        width: 1680,
        height: scrollHeight + 'px',
        printBackground: true,
        pageRanges: '1',
      });
    } else if (format === FORMAT.PNG) {
      buffer = await page.screenshot({
        fullPage: true,
      });
    } else if (format === FORMAT.CSV) {
      await page.click('button[id="downloadReport"]');
      await new Promise(resolve => setTimeout(resolve, 1000));
      const is_enabled = await page.evaluate(() => document.querySelector('#generateCSV[disabled]') == null);

      // Check if generateCSV button is enabled.
      if (is_enabled) {
        let catcher = page.waitForResponse(r => r.request().url().includes('/api/reporting/generateReport'));
        page.click('button[id="generateCSV"]');
        let response = await catcher;
        let payload = await response.json();
        buffer = payload.data;
      } else {
        spinner.fail('Please save search and retry');
        process.exit(1);
      }
    }

    await browser.close();

    const fileName = `${filename}.${format}`;
    const curTime = new Date();
    const timeCreated = curTime.valueOf();
    const data = { timeCreated, dataUrl: buffer.toString('base64'), fileName };

    await readStreamToFile(data.dataUrl, fileName, format);
    spinner.succeed('The report is downloaded');
  } catch (e) {
    spinner.fail('Downloading report failed. ' + e);
    process.exit(1);
  }
}

const waitForDynamicContent = async (
  page,
  timeout = 30000,
  interval = 1000,
  checks = 5
) => {
  const maxChecks = timeout / interval;
  let passedChecks = 0;
  let previousLength = 0;

  let i = 0;
  while (i++ <= maxChecks) {
    let pageContent = await page.content();
    let currentLength = pageContent.length;

    previousLength === 0 || previousLength != currentLength
      ? (passedChecks = 0)
      : passedChecks++;
    if (passedChecks >= checks) {
      break;
    }

    previousLength = currentLength;
    await new Promise(resolve => setTimeout(resolve, interval));
  }
};

const getReportSourceFromURL = (url) => {
  if (url.includes(URL_SOURCE.DASHBOARDS)) {
    return REPORT_TYPE.DASHBOARD;
  }
  else if (url.includes(URL_SOURCE.VISUALIZE)) {
    return REPORT_TYPE.VISUALIZATION;
  }
  else if (url.includes(URL_SOURCE.DISCOVER)) {
    return REPORT_TYPE.DISCOVER;
  }
  else if (url.includes(URL_SOURCE.NOTEBOOKS)) {
    return REPORT_TYPE.NOTEBOOK;
  }
  return REPORT_TYPE.OTHER;
}

const getUrl = async (url) => {
  let urlExt = url.split("#");
  let urlRef = "#" + urlExt[1];
  return urlRef;
};

const basicAuthentication = async (page, overridePage, url, username, password, tenant) => {
  await page.goto(url, { waitUntil: 'networkidle0' });
  await new Promise(resolve => setTimeout(resolve, 10000));
  await page.type('input[data-test-subj="user-name"]', username);
  await page.type('[data-test-subj="password"]', password);
  await page.click('button[type=submit]');
  await page.waitForTimeout(10000);
  try {
    if (tenant === 'global' || tenant === 'private') {
      await page.click('label[for=' + tenant + ']');
    } else {
      await page.click('label[for="custom"]');
      await page.click('button[data-test-subj="comboBoxToggleListButton"]');
      await page.type('input[data-test-subj="comboBoxSearchInput"]', tenant);
    }
  }
  catch (err) {
    spinner.fail('Invalid username or password');
    exit(1);
  }

  await page.waitForTimeout(2000);
  await page.click('button[data-test-subj="confirm"]');
  await page.waitForTimeout(25000);
  await overridePage.goto(url, { waitUntil: 'networkidle0' });
  await overridePage.waitForTimeout(5000);

  // Check if tenant was selected successfully.
  if ((await overridePage.$('button[data-test-subj="confirm"]')) !== null) {
    spinner.fail('Invalid tenant');
    exit(1);
  }
  await page.goto(url, { waitUntil: 'networkidle0' });
};

const samlAuthentication = async (page, url, username, password, tenant) => {
  await page.goto(url, { waitUntil: 'networkidle0' });
  await new Promise(resolve => setTimeout(resolve, 10000));
  let refUrl;
  await getUrl(url).then((value) => {
    refUrl = value;
  });
  await page.type('[name="identifier"]', username);
  await page.type('[name="credentials.passcode"]', password);
  await page.click('[value="Sign in"]')
  await page.waitForTimeout(30000);
  try {
    if (tenant === 'global' || tenant === 'private') {
      await page.click('label[for=' + tenant + ']');
    } else {
      await page.click('label[for="custom"]');
      await page.click('button[data-test-subj="comboBoxToggleListButton"]');
      await page.type('input[data-test-subj="comboBoxSearchInput"]', tenant);
    }
  }
  catch (err) {
    spinner.fail('Invalid username or password');
    exit(1);
  }
  await page.waitForTimeout(2000);
  await page.click('button[data-test-subj="confirm"]');
  await page.waitForTimeout(25000);
  await page.click(`a[href='${refUrl}']`);
}

const cognitoAuthentication = async (page, overridePage, url, username, password, tenant) => {
  await page.goto(url, { waitUntil: 'networkidle0' });
  await new Promise(resolve => setTimeout(resolve, 10000));
  await page.type('[name="username"]', username);
  await page.type('[name="password"]', password);
  await page.click('[name="signInSubmitButton"]');
  await page.waitForTimeout(30000);
  try {
    if (tenant === 'global' || tenant === 'private') {
      await page.click('label[for=' + tenant + ']');
    } else {
      await page.click('label[for="custom"]');
      await page.click('button[data-test-subj="comboBoxToggleListButton"]');
      await page.type('input[data-test-subj="comboBoxSearchInput"]', tenant);
    }
  }
  catch (err) {
    spinner.fail('Invalid username or password');
    exit(1);
  }
  await page.waitForTimeout(2000);
  await page.click('button[data-test-subj="confirm"]');
  await page.waitForTimeout(25000);
  await overridePage.goto(url, { waitUntil: 'networkidle0' });
  await overridePage.waitForTimeout(5000);

  // Check if tenant was selected successfully.
  if ((await overridePage.$('button[data-test-subj="confirm"]')) !== null) {
    spinner.fail('Invalid tenant');
    exit(1);
  }
  await page.goto(url, { waitUntil: 'networkidle0' });
}

const readStreamToFile = async (
  stream,
  fileName,
  format
) => {
  if (format === FORMAT.PDF || format === FORMAT.PNG) {
    let base64Image = stream.split(';base64,').pop();
    fs.writeFile(fileName, base64Image, { encoding: 'base64' }, function (err) {
      if (err) throw err;
    })
  } else {
    fs.writeFile(fileName, stream, function (err) {
      if (err) throw err;
    })
  }

};
