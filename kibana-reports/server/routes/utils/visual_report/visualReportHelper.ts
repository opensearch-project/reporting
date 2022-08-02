/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import puppeteer, { ElementHandle, SetCookie } from 'puppeteer-core';
import createDOMPurify from 'dompurify';
import { JSDOM } from 'jsdom';
import { Logger } from '../../../../../../src/core/server';
import {
  DEFAULT_REPORT_HEADER,
  REPORT_TYPE,
  FORMAT,
  SELECTOR,
  CHROMIUM_PATH,
} from '../constants';
import { getFileName } from '../helpers';
import { CreateReportResultType } from '../types';
import { ReportParamsSchemaType, VisualReportSchemaType } from 'server/model';
import { converter, replaceBlockedKeywords } from '../constants';
import fs from 'fs';
import cheerio from 'cheerio';

export const createVisualReport = async (
  reportParams: ReportParamsSchemaType,
  queryUrl: string,
  logger: Logger,
  cookie?: SetCookie,
  timezone?: string
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
      await page.waitFor(interval);
    }
  };

  // set up puppeteer
  const browser = await puppeteer.launch({
    headless: true,
    /**
     * TODO: temp fix to disable sandbox when launching chromium on Linux instance
     * https://github.com/puppeteer/puppeteer/blob/main/docs/troubleshooting.md#setting-up-chrome-linux-sandbox
     */
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-gpu', '--no-zygote', '--single-process'],
    executablePath: CHROMIUM_PATH,
    env: {
      TZ: timezone || 'UTC',
    },
  });
  const page = await browser.newPage();

  await page.setRequestInterception(true);
  page.on('request', (req) => {
    // disallow non-localhost redirections
    if (
      req.isNavigationRequest() &&
      req.redirectChain().length > 0 &&
      !/^(0|0.0.0.0|127.0.0.1|localhost)$/.test(new URL(req.url()).hostname)
    ) {
      logger.error(
        'Reporting does not allow redirections to outside of localhost, aborting. URL received: ' +
          req.url()
      );
      req.abort();
    } else {
      req.continue();
    }
  });

  page.setDefaultNavigationTimeout(0);
  page.setDefaultTimeout(100000); // use 100s timeout instead of default 30s
  if (cookie) {
    logger.info('domain enables security, use session cookie to access');
    await page.setCookie(cookie);
  }
  logger.info(`original queryUrl ${queryUrl}`);
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
  await page.waitFor(1000);
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
    default:
      throw Error(
        `report source can only be one of [Dashboard, Visualization]`
      );
  }

  // wait for dynamic page content to render
  await waitForDynamicContent(page);

  const screenshot = await page.screenshot({ fullPage: true });

  const templateHtml = composeReportHtml(
    keywordFilteredHeader,
    keywordFilteredFooter,
    screenshot.toString('base64')
  );
  await page.setContent(templateHtml);

  const numDisallowedTags = await page.evaluate(
    () =>
      document.getElementsByTagName('iframe').length +
      document.getElementsByTagName('embed').length +
      document.getElementsByTagName('object').length
  );
  if (numDisallowedTags > 0) {
    throw Error('Reporting does not support "iframe", "embed", or "object" tags, aborting');
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

const composeReportHtml = (
  header: string,
  footer: string,
  screenshot: string
) => {
  const $ = cheerio.load(fs.readFileSync(`${__dirname}/report_template.html`), {
    decodeEntities: false,
  });

  $('.reportWrapper img').attr('src', `data:image/png;base64,${screenshot}`);
  $('#reportingHeader > div.mde-preview > div.mde-preview-content').html(
    header
  );
  if (footer === '') {
    $('#reportingFooter').attr('hidden', 'true');
  } else {
    $('#reportingFooter > div.mde-preview > div.mde-preview-content').html(
      footer
    );
  }

  return $.root().html() || '';
};
