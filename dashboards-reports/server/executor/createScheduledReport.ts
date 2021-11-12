/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import {
  REPORT_TYPE,
  REPORT_STATE,
  LOCAL_HOST,
  DELIVERY_TYPE,
} from '../routes/utils/constants';
import { ILegacyClusterClient, Logger } from '../../../../src/core/server';
import { createSavedSearchReport } from '../routes/utils/savedSearchReportHelper';
import { ReportSchemaType } from '../model';
import { CreateReportResultType } from '../routes/utils/types';
import { createVisualReport } from '../routes/utils/visual_report/visualReportHelper';
import { updateReportState } from '../routes/lib/updateReportState';

export const createScheduledReport = async (
  reportId: string,
  report: ReportSchemaType,
  opensearchClient: ILegacyClusterClient,
  opensearchReportsClient: ILegacyClusterClient,
  logger: Logger
) => {
  let createReportResult: CreateReportResultType;

  const {
    report_definition: {
      report_params: reportParams,
      delivery: { delivery_type: deliveryType },
    },
  } = report;
  const { report_source: reportSource } = reportParams;

  try {
    // TODO: generate report logic will be added back once we have the user impersonation
    // if (reportSource === REPORT_TYPE.savedSearch) {
    //   createReportResult = await createSavedSearchReport(
    //     report,
    //     opensearchClient,
    //     isScheduledTask
    //   );
    // } else {
    //   // report source can only be one of [saved search, visualization, dashboard]
    //   // compose url with localhost
    //   const completeQueryUrl = `${LOCAL_HOST}${report.query_url}`;
    //   createReportResult = await createVisualReport(
    //     reportParams,
    //     completeQueryUrl,
    //     logger
    //   );
    // }

    await updateReportState(reportId, opensearchReportsClient, REPORT_STATE.created);
  } catch (error) {
    // update report instance with "error" state
    //TODO: save error detail and display on UI
    logger.error(`Failed to create scheduled report ${error}`);
    await updateReportState(reportId, opensearchReportsClient, REPORT_STATE.error);
  }
};
