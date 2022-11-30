/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  REPORT_TYPE,
  DATA_REPORT_CONFIG,
  EXTRA_HEADERS,
} from '../utils/constants';

import {
  ILegacyScopedClusterClient,
  OpenSearchDashboardsRequest,
  Logger,
  RequestHandlerContext,
} from '../../../../../src/core/server';
import { createSavedSearchReport } from '../utils/savedSearchReportHelper';
import { ReportSchemaType } from '../../model';
import { CreateReportResultType } from '../utils/types';
import { createVisualReport } from '../utils/visual_report/visualReportHelper';
import { saveReport } from './saveReport';
import { SemaphoreInterface } from 'async-mutex';
import { ReportingConfig } from 'server';
import _ from 'lodash';

export const createReport = async (
  request: OpenSearchDashboardsRequest,
  context: RequestHandlerContext,
  report: ReportSchemaType,
  config: ReportingConfig,
  savedReportId?: string
): Promise<CreateReportResultType> => {
  const isScheduledTask = false;
  //@ts-ignore
  const logger: Logger = context.reporting_plugin.logger;
  //@ts-ignore
  const semaphore: SemaphoreInterface = context.reporting_plugin.semaphore;
  // @ts-ignore
  const opensearchReportsClient: ILegacyScopedClusterClient = context.reporting_plugin.opensearchReportsClient.asScoped(
    request
  );
  const opensearchClient = context.core.opensearch.legacy.client;
  // @ts-ignore
  const timezone = request.query.timezone;
  // @ts-ignore
  const dateFormat =
    request.query.dateFormat || DATA_REPORT_CONFIG.excelDateFormat;
  // @ts-ignore
  const csvSeparator = request.query.csvSeparator || ',';
  // @ts-ignore
  const allowLeadingWildcards = !!request.query.allowLeadingWildcards;

  const protocol = config.get('osd_server', 'protocol');
  const hostname = config.get('osd_server', 'hostname');
  const port = config.get('osd_server', 'port');
  const basePath = config.osdConfig.get('server', 'basePath');

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
        allowLeadingWildcards,
        isScheduledTask,
        logger
      );
    } else {
      // report source can only be one of [saved search, visualization, dashboard, notebook]
      // compose url
      const relativeUrl = report.query_url.startsWith(basePath)
        ? report.query_url
        : `${basePath}${report.query_url}`;
      const completeQueryUrl = `${protocol}://${hostname}:${port}${relativeUrl}`;
      const extraHeaders = _.pick(request.headers, EXTRA_HEADERS);

      const [value, release] = await semaphore.acquire();
      try {
        createReportResult = await createVisualReport(
          reportParams,
          completeQueryUrl,
          logger,
          extraHeaders,
          timezone
        );
      } finally {
        release();
      }
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
