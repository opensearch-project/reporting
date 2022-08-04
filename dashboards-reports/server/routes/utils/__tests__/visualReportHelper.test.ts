/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import 'regenerator-runtime/runtime';
import { createVisualReport } from '../visual_report/visualReportHelper';
import { Logger } from '../../../../../../src/core/server';
import { ReportParamsSchemaType, reportSchema } from '../../../model';
import { mockLogger } from '../../../../test/__mocks__/loggerMock';

const mockHeader = { mockKey: 'mockValue' };
const input = {
  query_url: '/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d',
  time_from: 1343576635300,
  time_to: 1596037435301,
  report_definition: {
    report_params: {
      report_name: 'test visual report',
      report_source: 'Dashboard',
      description: 'Hi this is your Dashboard on demand',
      core_params: {
        base_url: '/app/dashboards#/view/7adfa750-4c81-11e8-b3d7-01146121b73d',
        window_width: 1300,
        window_height: 900,
        report_format: 'png',
        time_duration: 'PT5M',
        origin: 'http://localhost:5601',
      },
    },
    delivery: {
      configIds: [],
      title: 'title',
      textDescription: 'text description',
      htmlDescription: 'html description',
    },
    trigger: {
      trigger_type: 'On demand',
    },
  },
};

const mockHtmlPath = `file://${__dirname}/demo_dashboard.html`;

describe('test create visual report', () => {
  test('create report with valid input', async () => {
    // Check if the assumption of input is up-to-date
    reportSchema.validate(input);
  }, 20000);

  test('create png report', async () => {
    expect.assertions(3);
    const reportParams = input.report_definition.report_params;
    const { dataUrl, fileName } = await createVisualReport(
      reportParams as ReportParamsSchemaType,
      mockHtmlPath,
      mockLogger,
      mockHeader,
      undefined,
      /^(data:image|file:\/\/)/
    );
    expect(fileName).toContain(`${reportParams.report_name}`);
    expect(fileName).toContain('.png');
    expect(dataUrl).toBeDefined();
  }, 60000);

  test('create pdf report', async () => {
    expect.assertions(3);
    const reportParams = input.report_definition.report_params;
    reportParams.core_params.report_format = 'pdf';

    const { dataUrl, fileName } = await createVisualReport(
      reportParams as ReportParamsSchemaType,
      mockHtmlPath,
      mockLogger,
      mockHeader,
      undefined,
      /^(data:image|file:\/\/)/
    );
    expect(fileName).toContain(`${reportParams.report_name}`);
    expect(fileName).toContain('.pdf');
    expect(dataUrl).toBeDefined();
  }, 60000);
});
