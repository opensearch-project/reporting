/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { v1 as uuidv1 } from 'uuid';
import { ReportSchemaType } from '../../../server/model';
import { uiSettingsService } from '../utils/settings_service';

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

export const generateReport = async (id: string, forceDelay = 15000) => {
  console.log('❗id:', id);
  const http = uiSettingsService.getHttpClient();

  const report = await http.get<ReportSchemaType>(
    '../api/reporting/reports/' + id
  );
  console.log('❗report:', report);
  const format =
    report.report_definition.report_params.core_params.report_format;
  const fileName =
    report.report_definition.report_params.report_name +
    `_${new Date().toISOString()}_${uuidv1()}.${format}`;
  await timeout(1000);
  // TODO should fail if reached max timeout or not on right page
  // await waitForSelector('#dashboardViewport');
  await timeout(forceDelay);

  const width = document.documentElement.scrollWidth;
  const height = document.documentElement.scrollHeight;
  return html2canvas(document.body, {
    windowWidth: width,
    windowHeight: height,
    imageTimeout: 30000,
    useCORS: true,
    removeContainer: false,
  }).then(function (canvas) {
    // TODO remove this and 'removeContainer: false' when https://github.com/niklasvh/html2canvas/pull/2949 is merged
    document.querySelectorAll('.html2canvas-container').forEach((e) => {
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
      const pdf = new jsPDF('p', 'pt', [width, height]);
      pdf.addImage(canvas, 'JPEG', 0, 0, width, height);
      pdf.save(fileName);
    }
    return true;
  });
};
