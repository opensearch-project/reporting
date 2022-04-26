/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import puppeteer from 'puppeteer-core';
import createDOMPurify from 'dompurify';
import { JSDOM } from 'jsdom';
import { CHROMIUM_PATH, REPORT_TYPE_URLS, FORMAT, REPORT_TYPE } from './constants.js';


export async function downloadVisualReport(url, type, object_id, format) {
  const window = new JSDOM('').window;
  const DOMPurify = createDOMPurify(window);

  try {
    const browser = await puppeteer.launch({
      headless: true,
      /**
       * TODO: temp fix to disable sandbox when launching chromium on Linux instance
       * https://github.com/puppeteer/puppeteer/blob/main/docs/troubleshooting.md#setting-up-chrome-linux-sandbox
       */
      args: [
        '--no-sandbox',
        '--disable-setuid-sandbox',
        '--disable-gpu',
        '--no-zygote',
        '--single-process',
        '--font-render-hinting=none',
      ],
      executablePath: CHROMIUM_PATH,
      ignoreHTTPSErrors: true,
      env: {
        TZ: 'UTC', // leave as UTC for now
      },
    });

    const page = await browser.newPage();
    page.setDefaultNavigationTimeout(0);
    page.setDefaultTimeout(100000);

    let queryUrl = '';
    if (url != undefined) {
      queryUrl = url;
    }
    else {
      queryUrl = 'localhost:5601/app/';
      // create URL from report source type and saved object ID
      if (type.toUpperCase() === 'DASHBOARD') {
        queryUrl += REPORT_TYPE_URLS.DASHBOARD;
      }
      else if (type.toUpperCase() === 'VISUALIZATION') {
        queryUrl += REPORT_TYPE_URLS.VISUALIZATION;
      }
      else if (type.toUpperCase() === 'SAVED SEARCH') {
        queryUrl += REPORT_TYPE_URLS.SAVED_SEARCH;
      }
      else if (type.toUpperCase() === 'NOTEBOOK') {
        queryUrl += REPORT_TYPE_URLS.NOTEBOOK;
      }
      queryUrl += '/' + object_id;
    }
    await page.goto(queryUrl, { waitUntil: 'networkidle0' });
    await page.setViewport({
      width: 1680, // temp constants
      height: 2560,
    });


    const testGoogle = await browser.newPage();
    await testGoogle.goto('https://www.google.com', {waitUntil: 'networkidle0'});
    await testGoogle.evaluate(`() => {
	document.title = 'New title';
	
    }`);
    const reportSource = getReportSourceFromURL(url);
    console.log('report source is', reportSource);
    console.log('evaluated google');
    // if its a report 
    await page.evaluate(
      (reportSource, REPORT_TYPE) => {
        // remove buttons
	console.log('in evaluate');
        document
          .querySelectorAll("[class^='euiButton']")
          .forEach((e) => e.remove());
        // remove top navBar
	console.log('after first document statement');
        document
          .querySelectorAll("[class^='euiHeader']")
          .forEach((e) => e.remove());
	console.log('after second document statement');
        // remove visualization editor
        if (reportSource === REPORT_TYPE.VISUALIZATION) {
          document
            .querySelector('[data-test-subj="splitPanelResizer"]')
            ?.remove();
          document.querySelector('.visEditor__collapsibleSidebar')?.remove();
        }
	console.log('after third doc statement');
        document.body.style.paddingTop = '0px';
	console.log('at end of evaluate');
      },
      reportSource,
      REPORT_TYPE
    );
    
      // force wait for any resize to load after the above DOM modification
      await page.waitFor(1000);
      switch (reportSource) {
        case 'Dashboard':
          await page.waitForSelector(SELECTOR.dashboard, {
            visible: true,
          });
          break;
        case 'Visualization':
          await page.waitForSelector(SELECTOR.visualization, {
            visible: true,
          });
          break;
        case 'Notebook':
          await page.waitForSelector(SELECTOR.notebook, {
            visible: true,
          });
          break;
        default:
          throw Error(
            `report source can only be one of [Dashboard, Visualization]`
          );
      }

      await waitForDynamicContent(page);
  } catch (e) {
    console.log('error is', e);
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
    await page.waitFor(interval);
  }

  let buffer;
  // create pdf or png accordingly
  if (format === FORMAT.PDF) {
    const scrollHeight = await page.evaluate(
      /* istanbul ignore next */
      () => document.documentElement.scrollHeight
    );

    buffer = await page.pdf({
      margin: undefined,
      width: windowWidth,
      height: scrollHeight + 'px',
      printBackground: true,
      pageRanges: '1',
    });
  } else if (format === FORMAT.PNG) {
    buffer = await page.screenshot({
      fullPage: true,
    });
  }

  const curTime = new Date();
  const timeCreated = curTime.valueOf();
  await browser.close();
  return { timeCreated, dataUrl: buffer.toString('base64'), fileName };
};

const getReportSourceFromURL = (url) => {
  if (url.includes('dashboards')) {
    return 'Dashboard';
  }
  else if (url.includes('visualize')) {
    return 'Visualization';
  }
  else if (url.includes('discover')) {
    return 'Saved search';
  }
  else if (url.includes('notebooks')) {
    return 'Notebook';
  }
}

function puppeteerEvaluate(reportSource) {
  // remove buttons
  document
    .querySelectorAll("[class^='euiButton']")
    .forEach((e) => e.remove());
  // remove top navBar
  document
    .querySelectorAll("[class^='euiHeader']")
    .forEach((e) => e.remove());
  // remove visualization editor
  if (reportSource === 'Visualization') {
    document
      .querySelector('[data-test-subj="splitPanelResizer"]')
      ?.remove();
    document.querySelector('.visEditor__collapsibleSidebar')?.remove();
  }
  document.body.style.paddingTop = '0px';
}
