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

import { isValidCron } from 'cron-validator';
import { i18n } from '@osd/i18n';
import moment from 'moment';
import { includeDelivery } from '../delivery/delivery';

export const definitionInputValidation = async (
  metadata,
  error,
  setShowSettingsReportNameError,
  setSettingsReportNameErrorMessage,
  setShowSettingsReportSourceError,
  setSettingsReportSourceErrorMessage,
  setShowTriggerIntervalNaNError,
  timeRange,
  setShowTimeRangeError,
  setShowCronError
) => {
  // check report name
  // allow a-z, A-Z, 0-9, (), [], ',' - and _ and spaces
  let regexp = /^[\w\-\s\(\)\[\]\,\_\-+]+$/;
  if (metadata.report_params.report_name.search(regexp) === -1) {
    setShowSettingsReportNameError(true);
    if (metadata.report_params.report_name === '') {
      setSettingsReportNameErrorMessage('Name must not be empty.');
    } else {
      setSettingsReportNameErrorMessage('Invalid characters in report name.');
    }
    error = true;
  }

  // if recurring by interval and input is not a number
  if (
    metadata.trigger.trigger_type === 'Schedule' &&
    metadata.trigger.trigger_params.schedule_type === 'Recurring'
  ) {
    let interval = parseInt(
      metadata.trigger.trigger_params.schedule.interval.period
    );
    if (isNaN(interval)) {
      setShowTriggerIntervalNaNError(true);
      error = true;
    }
  }

  // if report source is blank
  if (metadata.report_params.core_params.base_url === '') {
    setShowSettingsReportSourceError(true);
    setSettingsReportSourceErrorMessage(
      i18n.translate('opensearch.reports.error.reportSourceMustNotBeEmpty', {
        defaultMessage: 'Report source must not be empty.',
      })
    );
    error = true;
  }

  // if time range is invalid
  const nowDate = new Date(moment.now());
  if (timeRange.timeFrom > timeRange.timeTo || timeRange.timeTo > nowDate) {
    setShowTimeRangeError(true);
    error = true;
  }

  // if cron based and cron input is invalid
  if (
    metadata.trigger.trigger_type === 'Schedule' &&
    metadata.trigger.trigger_params.schedule_type === 'Cron based'
  ) {
    if (
      !isValidCron(metadata.trigger.trigger_params.schedule.cron.expression)
    ) {
      setShowCronError(true);
      error = true;
    }
  }
  return error;
};
