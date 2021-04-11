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

import { OpenSearchDashboardsResponseFactory } from '../../../../../src/core/server';
import { v1 as uuidv1 } from 'uuid';
import {
  ILegacyClusterClient,
  ILegacyScopedClusterClient,
} from '../../../../../src/core/server';

/**
 * OpenSearch error response body:
 *  {
 *   error: {
 *     root_cause: [{ type: 'status_exception', reason: 'test exception' }],
 *     type: 'status_exception',
 *     reason: 'test exception',
 *   },
 *   status: 404,
 * };
 *
 */
export function parseOpenSearchErrorResponse(error: any) {
  if (error.response) {
    try {
      const opensearchErrorResponse = JSON.parse(error.response);
      return opensearchErrorResponse.error.reason || error.response;
    } catch (parsingError) {
      return error.response;
    }
  }
  return error.message;
}

export function errorResponse(response: OpenSearchDashboardsResponseFactory, error: any) {
  return response.custom({
    statusCode: error.statusCode || 500,
    body: parseOpenSearchErrorResponse(error),
  });
}

/**
 * Generate report file name based on name and timestamp.
 * @param itemName      report item name
 * @param timeCreated   timestamp when this is being created
 */
export function getFileName(itemName: string, timeCreated: Date): string {
  return `${itemName}_${timeCreated.toISOString()}_${uuidv1()}`;
}

/**
 * Call OpenSearch cluster function.
 * @param client    OpenSearch client
 * @param endpoint  OpenSearch API method
 * @param params    OpenSearch API parameters
 */
export const callCluster = async (
  client: ILegacyClusterClient | ILegacyScopedClusterClient,
  endpoint: string,
  params: any,
  isScheduledTask: boolean
) => {
  let opensearchResp;
  if (isScheduledTask) {
    opensearchResp = await (client as ILegacyClusterClient).callAsInternalUser(
      endpoint,
      params
    );
  } else {
    opensearchResp = await (client as ILegacyScopedClusterClient).callAsCurrentUser(
      endpoint,
      params
    );
  }
  return opensearchResp;
};

export const checkErrorType = (error: any) => {
  if (error.statusCode && Math.floor(error.statusCode / 100) === 4) {
    return 'user_error';
  } else {
    return 'system_error';
  }
};
