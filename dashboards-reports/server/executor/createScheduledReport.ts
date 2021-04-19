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
  LOCAL_HOST,
  DELIVERY_TYPE,
} from '../routes/utils/constants';
import { ILegacyClusterClient, Logger } from '../../../../src/core/server';
import { createSavedSearchReport } from '../routes/utils/savedSearchReportHelper';
import { ReportSchemaType } from '../model';
import { CreateReportResultType } from '../routes/utils/types';
import { createVisualReport } from '../routes/utils/visual_report/visualReportHelper';
import { deliverReport } from '../routes/lib/deliverReport';
import { updateReportState } from '../routes/lib/updateReportState';

export const createScheduledReport = async (
  reportId: string,
  report: ReportSchemaType,
  opensearchClient: ILegacyClusterClient,
  opensearchReportsClient: ILegacyClusterClient,
  notificationClient: ILegacyClusterClient,
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

    // deliver report
    if (deliveryType == DELIVERY_TYPE.channel) {
      await deliverReport(
        report,
        notificationClient,
        opensearchReportsClient,
        reportId,
        logger
      );
    }
  } catch (error) {
    // update report instance with "error" state
    //TODO: save error detail and display on UI
    logger.error(`Failed to create scheduled report ${error}`);
    await updateReportState(reportId, opensearchReportsClient, REPORT_STATE.error);
  }
};
