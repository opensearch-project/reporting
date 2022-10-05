/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import puppeteer, { Headers } from 'puppeteer-core';
import createDOMPurify from 'dompurify';
import { JSDOM } from 'jsdom';
import { Logger } from '../../../../../../src/core/server';
import {
  DEFAULT_REPORT_HEADER,
  REPORT_TYPE,
  FORMAT,
  SELECTOR,
  CHROMIUM_PATH,
  SECURITY_CONSTANTS,
  ALLOWED_HOSTS,
} from '../constants';
import { getFileName } from '../helpers';
import { CreateReportResultType } from '../types';
import { ReportParamsSchemaType, VisualReportSchemaType } from 'server/model';
import { converter, replaceBlockedKeywords } from '../constants';
import fs from 'fs';
import _ from 'lodash';

export const createVisualReport = async (
  reportParams: ReportParamsSchemaType,
  queryUrl: string,
  logger: Logger,
  extraHeaders: Headers,
  timezone?: string,
  validRequestProtocol = /^(data:image)/,
): Promise<CreateReportResultType> => {
  const {
    core_params,
    report_name: reportName,
    report_source: reportSource,
  } = reportParams;
  const coreParams = core_params as VisualReportSchemaType;
  const {
    header,
    footer,
    window_height: windowHeight,
    window_width: windowWidth,
    report_format: reportFormat,
  } = coreParams;

  const window = new JSDOM('').window;
  const DOMPurify = createDOMPurify(window);

  let keywordFilteredHeader = header 
    ? converter.makeHtml(header) 
    : DEFAULT_REPORT_HEADER;
  let keywordFilteredFooter = footer ? converter.makeHtml(footer) : '';

  keywordFilteredHeader = DOMPurify.sanitize(keywordFilteredHeader);
  keywordFilteredFooter = DOMPurify.sanitize(keywordFilteredFooter);

  // filter blocked keywords in header and footer
  if (keywordFilteredHeader !== '') {
    keywordFilteredHeader = replaceBlockedKeywords(keywordFilteredHeader);
  }
  if (keywordFilteredFooter !== '') {
    keywordFilteredFooter = replaceBlockedKeywords(keywordFilteredFooter);
  }

  // set up puppeteer
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
      '--font-render-hinting=none',
      '--js-flags="--jitless --no-opt"',
      '--disable-features=V8OptimizeJavascript',
    ],
    executablePath: CHROMIUM_PATH,
    ignoreHTTPSErrors: true,
    env: {
      TZ: timezone || 'UTC',
    },
    pipe: true,
  });
  const page = await browser.newPage();

  await page.setRequestInterception(true);
  let localStorageAvailable = true;
  page.on('request', (req) => {
    // disallow non-allowlisted connections. urls with valid protocols do not need ALLOWED_HOSTS check
    if (
      !validRequestProtocol.test(req.url()) &&
      !ALLOWED_HOSTS.test(new URL(req.url()).hostname)
    ) {
      if (req.isNavigationRequest() && req.redirectChain().length > 0) {
        localStorageAvailable = false;
        logger.error(
          'Reporting does not allow redirections to outside of localhost, aborting. URL received: ' +
            req.url()
        );
      } else {
        logger.warn(
          'Disabled connection to non-allowlist domains: ' + req.url()
        );
      }
      req.abort();
    } else {
      req.continue();
    }
  });

  page.setDefaultNavigationTimeout(0);
  page.setDefaultTimeout(300000); // use 300s timeout instead of default 30s
  // Set extra headers that are needed
  if (!_.isEmpty(extraHeaders)) {
    await page.setExtraHTTPHeaders(extraHeaders);
  }
  logger.info(`original queryUrl ${queryUrl}`);
  await page.goto(queryUrl, { waitUntil: 'networkidle0' });
  // should add to local storage after page.goto, then access the page again - browser must have an url to register local storage item on it
  try {
    await page.evaluate(
      /* istanbul ignore next */
      (key) => {
        try {
          if (
            localStorageAvailable &&
            typeof localStorage !== 'undefined' &&
            localStorage !== null
          ) {
            localStorage.setItem(key, 'false');
          }
        } catch (err) {}
      },
      SECURITY_CONSTANTS.TENANT_LOCAL_STORAGE_KEY
    );
  } catch (err) {
    logger.error(err);
  }
  await page.goto(queryUrl, { waitUntil: 'networkidle0' });
  logger.info(`page url ${page.url()}`);

  await page.setViewport({
    width: windowWidth,
    height: windowHeight,
  });

  let buffer: Buffer;
  // remove unwanted elements
  await page.evaluate(
    /* istanbul ignore next */
    (reportSource, REPORT_TYPE) => {
      // remove buttons
      document
        .querySelectorAll("[class^='euiButton']")
        .forEach((e) => e.remove());
      // remove top navBar
      document
        .querySelectorAll("[class^='euiHeader']")
        .forEach((e) => e.remove());
      // remove visualization editor
      if (reportSource === REPORT_TYPE.visualization) {
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

  // force wait for any resize to load after the above DOM modification
  await new Promise(resolve => setTimeout(resolve, 1000));
  // crop content
  switch (reportSource) {
    case REPORT_TYPE.dashboard:
      await page.waitForSelector(SELECTOR.dashboard, {
        visible: true,
      });
      break;
    case REPORT_TYPE.visualization:
      await page.waitForSelector(SELECTOR.visualization, {
        visible: true,
      });
      break;
    case REPORT_TYPE.notebook:
      await page.waitForSelector(SELECTOR.notebook, {
        visible: true,
      });
      break;
    default:
      throw Error(
        `report source can only be one of [Dashboard, Visualization]`
      );
  }

  // wait for dynamic page content to render
  await waitForDynamicContent(page);

  await addReportHeader(page, keywordFilteredHeader);
  await addReportFooter(page, keywordFilteredFooter);
  await addReportStyle(page);

  // this causes UT to fail in github CI but works locally
  try {
    const numDisallowedTags = await page.evaluate(
      () =>
        document.getElementsByTagName('iframe').length +
        document.getElementsByTagName('embed').length +
        document.getElementsByTagName('object').length
    );
    if (numDisallowedTags > 0) {
      throw Error('Reporting does not support "iframe", "embed", or "object" tags, aborting');
    }
  } catch (error) {
    logger.error(error);
  }

  // create pdf or png accordingly
  if (reportFormat === FORMAT.pdf) {
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
  } else if (reportFormat === FORMAT.png) {
    buffer = await page.screenshot({
      fullPage: true,
    });
  }

  const curTime = new Date();
  const timeCreated = curTime.valueOf();
  const fileName = `${getFileName(reportName, curTime)}.${reportFormat}`;
  await browser.close();

  return { timeCreated, dataUrl: buffer.toString('base64'), fileName };
};

const addReportStyle = async (page: puppeteer.Page) => {
  const css = fs.readFileSync(`${__dirname}/style.css`).toString();

  await page.evaluate(
    /* istanbul ignore next */
    (style: string) => {
      const styleElement = document.createElement('style');
      styleElement.innerHTML = style;
      document.getElementsByTagName('head')[0].appendChild(styleElement);
    },
    css
  );
};

const addReportHeader = async (page: puppeteer.Page, header: string) => {
  const headerHtml = fs
    .readFileSync(`${__dirname}/header_template.html`)
    .toString()
    .replace('<!--CONTENT-->', header);

  await page.evaluate(
    /* istanbul ignore next */
    (headerHtml: string) => {
      const content = document.body.firstChild;
      const headerContainer = document.createElement('div');
      headerContainer.className = 'reportWrapper';
      headerContainer.innerHTML = headerHtml;
      content?.parentNode?.insertBefore(headerContainer, content);
    },
    headerHtml
  );
};

const addReportFooter = async (page: puppeteer.Page, footer: string) => {
  const headerHtml = fs
    .readFileSync(`${__dirname}/footer_template.html`)
    .toString()
    .replace('<!--CONTENT-->', footer);

  await page.evaluate(
    /* istanbul ignore next */
    (headerHtml: string) => {
      const content = document.body.firstChild;
      const headerContainer = document.createElement('div');
      headerContainer.className = 'reportWrapper';
      headerContainer.innerHTML = headerHtml;
      content?.parentNode?.insertBefore(headerContainer, null);
    },
    headerHtml
  );
};

// add waitForDynamicContent function
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
