/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { v1 as uuidv1 } from 'uuid';
import { ReportSchemaType } from '../../../server/model';
import { uiSettingsService } from '../utils/settings_service';
import { reportingStyle } from './assets/report_styles';
import { converter, DEFAULT_REPORT_HEADER, REPORT_TYPE } from './constants';

const waitForSelector = (selector: string) => {
  return new Promise((resolve) => {
    if (document.querySelector(selector)) {
      return resolve(document.querySelector(selector));
    }
    const observer = new MutationObserver((mutations) => {
      if (document.querySelector(selector)) {
        resolve(document.querySelector(selector));
        observer.disconnect();
      }
    });
    observer.observe(document.body, {
      childList: true,
      subtree: true,
    });
  });
};

const timeout = (ms: number) => {
  return new Promise((resolve) => setTimeout(resolve, ms));
};

const removeNonReportElements = (doc: Document, reportSource: REPORT_TYPE) => {
  // remove buttons
  doc.querySelectorAll("[class^='euiButton']").forEach((e) => e.remove());
  // remove top navBar
  doc.querySelectorAll("[class^='euiHeader']").forEach((e) => e.remove());
  // remove visualization editor
  if (reportSource === REPORT_TYPE.visualization) {
    doc.querySelector('[data-test-subj="splitPanelResizer"]')?.remove();
    doc.querySelector('.visEditor__collapsibleSidebar')?.remove();
  }
  doc.body.style.paddingTop = '0px';
};

const addReportHeader = (doc: Document, header: string) => {
  const headerHtml = `<div id="reportingHeader">
    <div class="mde-preview" data-testid="mde-preview">
      <div class="mde-preview-content">${header}</div>
    </div>
  </div>`;
  const headerContainer = document.createElement('div');
  headerContainer.className = 'reportWrapper';
  headerContainer.innerHTML = headerHtml;
  const body = doc.getElementsByTagName('body')[0];
  body.insertBefore(headerContainer, body.children[0]);
};

const addReportFooter = (doc: Document, footer: string) => {
  const footerHtml = `<div id="reportingFooter">
    <div class="mde-preview" data-testid="mde-preview">
      <div class="mde-preview-content">${footer}</div>
    </div>
  </div>`;
  const footerContainer = document.createElement('div');
  footerContainer.className = 'reportWrapper';
  footerContainer.innerHTML = footerHtml;
  const body = doc.getElementsByTagName('body')[0];
  body.appendChild(footerContainer);
};

const addReportStyle = (doc: Document, style: string) => {
  const styleElement = document.createElement('style');
  styleElement.innerHTML = style;
  doc.getElementsByTagName('head')[0].appendChild(styleElement);
};

const computeHeight = (height: number, header: string, footer: string) => {
  let computedHeight = height;
  const headerLines = header.split('\n').length;
  const footerLines = footer.split('\n').length;
  if (headerLines) {
    computedHeight += 24 * headerLines;
  }
  if (footerLines) {
    computedHeight += 50 + 24 * footerLines;
  }
  return computedHeight;
};

export const generateReport = async (id: string, forceDelay = 15000) => {
  const http = uiSettingsService.getHttpClient();

  const report = await http.get<ReportSchemaType>(
    '../api/reporting/reports/' + id
  );
  const format =
    report.report_definition.report_params.core_params.report_format;
  const reportSource = report.report_definition.report_params
    .report_source as REPORT_TYPE;
  const headerInput = report.report_definition.report_params.core_params.header;
  const footerInput = report.report_definition.report_params.core_params.footer;
  const header = headerInput
    ? converter.makeHtml(headerInput)
    : DEFAULT_REPORT_HEADER;
  const footer = footerInput ? converter.makeHtml(footerInput) : '';
  const fileName =
    report.report_definition.report_params.report_name +
    `_${new Date().toISOString()}_${uuidv1()}.${format}`;

  await timeout(4000);
  // TODO should fail if reached max timeout or not on right page
  // await waitForSelector('#dashboardViewport');
  // await timeout(forceDelay);

  const width = document.documentElement.scrollWidth;
  const height = computeHeight(
    document.documentElement.scrollHeight,
    header,
    footer
  );
  return html2canvas(document.body, {
    windowWidth: width,
    windowHeight: height,
    width,
    height,
    imageTimeout: 30000,
    useCORS: true,
    removeContainer: false,
    onclone: function (documentClone) {
      removeNonReportElements(documentClone, reportSource);
      addReportHeader(documentClone, header);
      addReportFooter(documentClone, footer);
      addReportStyle(documentClone, reportingStyle);
    },
  }).then(function (canvas) {
    // TODO remove this and 'removeContainer: false' when https://github.com/niklasvh/html2canvas/pull/2949 is merged
    document.querySelectorAll('.html2canvas-container').forEach((e: any) => {
      const iframe = e.contentWindow;
      if (e) {
        e.src = 'about:blank';
        iframe.document.write('');
        iframe.document.clear();
        iframe.close();
        e.remove();
      }
    });

    if (format === 'png') {
      const link = document.createElement('a');
      link.download = fileName;
      link.href = canvas.toDataURL();
      link.click();
    } else {
      const orient = canvas.width > canvas.height ? 'landscape' : 'portrait';
      const pdf = new jsPDF(orient, 'px', [canvas.width, canvas.height]);
      pdf.addImage(canvas, 'JPEG', 0, 0, canvas.width, canvas.height);
      pdf.save(fileName);
    }
    return true;
  });
};
