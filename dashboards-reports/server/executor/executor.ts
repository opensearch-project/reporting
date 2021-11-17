/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { ILegacyClusterClient, Logger } from '../../../../src/core/server';
import { createScheduledReport } from './createScheduledReport';
import { POLL_INTERVAL } from '../utils/constants';
import { parseOpenSearchErrorResponse } from '../routes/utils/helpers';
import { backendToUiReport } from '../routes/utils/converters/backendToUi';
import { BackendReportInstanceType } from 'server/model/backendModel';

async function pollAndExecuteJob(
  opensearchReportsClient: ILegacyClusterClient,
  opensearchClient: ILegacyClusterClient,
  logger: Logger
) {
  logger.info(
    `start polling at time: ${new Date().toISOString()} with fixed interval: ${POLL_INTERVAL} milliseconds`
  );
  try {
    // poll job
    const opensearchResp = await opensearchReportsClient.callAsInternalUser(
      'opensearch_reports.pollReportInstance'
    );
    const job: BackendReportInstanceType = opensearchResp.reportInstance;

    // job retrieved, otherwise will be undefined because 204 No-content is returned
    if (job) {
      const reportMetadata = backendToUiReport(job);
      const reportId = job.id;
      logger.info(
        `scheduled job sent from scheduler with report id: ${reportId}`
      );

      await createScheduledReport(
        reportId,
        reportMetadata,
        opensearchClient,
        opensearchReportsClient,
        logger
      );
    } else {
      // 204 no content is returned, 204 doesn't have response body
      logger.info(`No scheduled job to execute ${JSON.stringify(opensearchResp)}`);
    }
  } catch (error) {
    logger.error(
      `Failed to poll job ${error.statusCode} ${parseOpenSearchErrorResponse(error)}`
    );
  }
}

export { pollAndExecuteJob };
