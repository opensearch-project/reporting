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

export const PLUGIN_NAME = 'Reporting';
export const PLUGIN_ID = 'reports-dashboards';

export const API_PREFIX = '/api/reporting';

const BASE_REPORTS_URI = '/_plugins/_reports';

export const OPENSEARCH_REPORTS_API = {
  ON_DEMAND_REPORT: `${BASE_REPORTS_URI}/on_demand`,
  REPORT_INSTANCE: `${BASE_REPORTS_URI}/instance`,
  LIST_REPORT_INSTANCES: `${BASE_REPORTS_URI}/instances`,
  REPORT_DEFINITION: `${BASE_REPORTS_URI}/definition`,
  LIST_REPORT_DEFINITIONS: `${BASE_REPORTS_URI}/definitions`,
  POLL_REPORT_INSTANCE: `${BASE_REPORTS_URI}/poll_instance`,
};

const REPORTING_NOTIFICATIONS_API_PREFIX = '/api/reporting_notifications';
export const REPORTING_NOTIFICATIONS_DASHBOARDS_API = Object.freeze({
  GET_CONFIGS: `${REPORTING_NOTIFICATIONS_API_PREFIX}/get_configs`,
  GET_EVENT: `${REPORTING_NOTIFICATIONS_API_PREFIX}/get_event`,
  SEND_TEST_MESSAGE: `${REPORTING_NOTIFICATIONS_API_PREFIX}/test_message`
});

const NOTIFICATIONS_API_BASE_PATH = '/_plugins/_notifications';
export const NOTIFICATIONS_API = Object.freeze({
  CONFIGS: `${NOTIFICATIONS_API_BASE_PATH}/configs`,
  EVENTS: `${NOTIFICATIONS_API_BASE_PATH}/events`,
  TEST_MESSAGE: `${NOTIFICATIONS_API_BASE_PATH}/feature/test`
});