/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

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

import {
  REPORT_TYPE,
  REPORT_STATE,
  DELIVERY_TYPE,
  DATA_REPORT_CONFIG,
  EXTRA_HEADERS,
  LOCAL_HOST,
} from '../utils/constants';

import {
  ILegacyScopedClusterClient,
  OpenSearchDashboardsRequest,
  Logger,
  RequestHandlerContext,
} from '../../../../../src/core/server';
import { createSavedSearchReport } from '../utils/savedSearchReportHelper';
import { ReportSchemaType, VisualReportSchemaType } from '../../model';
import { CreateReportResultType } from '../utils/types';
import { saveReport } from './saveReport';
import _ from 'lodash';
import { getFileName } from '../utils/helpers';

export const createReport = async (
  request: OpenSearchDashboardsRequest,
  context: RequestHandlerContext,
  report: ReportSchemaType,
  savedReportId?: string
): Promise<CreateReportResultType> => {
  const isScheduledTask = false;
  //@ts-ignore
  const logger: Logger = context.reporting_plugin.logger;
  // @ts-ignore
  const opensearchReportsClient: ILegacyScopedClusterClient =
    context.reporting_plugin.opensearchReportsClient.asScoped(request);
  const opensearchClient = context.core.opensearch.legacy.client;
  // @ts-ignore
  const timezone = request.query.timezone;
  // @ts-ignore
  const dateFormat =
    request.query.dateFormat || DATA_REPORT_CONFIG.excelDateFormat;
  // @ts-ignore
  const csvSeparator = request.query.csvSeparator || ',';

  let createReportResult: CreateReportResultType;
  let reportId;

  const {
    report_definition: { report_params: reportParams },
  } = report;
  const { report_source: reportSource } = reportParams;

  try {
    // create new report instance and set report state to "pending"
    if (savedReportId) {
      reportId = savedReportId;
    } else {
      const opensearchResp = await saveReport(report, opensearchReportsClient);
      reportId = opensearchResp.reportInstance.id;
    }
    // generate report
    if (reportSource === REPORT_TYPE.savedSearch) {
      createReportResult = await createSavedSearchReport(
        report,
        opensearchClient,
        dateFormat,
        csvSeparator,
        isScheduledTask,
        logger
      );
    } else {
      // report source can only be one of [saved search, visualization, dashboard, notebook]
      // compose url
      const completeQueryUrl = `${LOCAL_HOST}${report.query_url}`;
      const extraHeaders = _.pick(request.headers, EXTRA_HEADERS);

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
      const curTime = new Date();
      const timeCreated = curTime.valueOf();
      const fileName = `${getFileName(reportName, curTime)}.${reportFormat}`;

      return { timeCreated, dataUrl: '', fileName, reportId, queryUrl: report.query_url };
    }
    // update report state to "created"
    // TODO: temporarily remove the following
    // if (!savedReportId) {
    //   await updateReportState(reportId, opensearchReportsClient, REPORT_STATE.created);
    // }
  } catch (error) {
    // update report instance with "error" state
    // TODO: save error detail and display on UI
    // TODO: temporarily disable the following, will add back
    // if (!savedReportId) {
    //   await updateReportState(reportId, opensearchReportsClient, REPORT_STATE.error);
    // }
    throw error;
  }

  return createReportResult;
};
