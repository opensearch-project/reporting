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

import moment from 'moment-timezone';
import { i18n } from '@osd/i18n';

export const TRIGGER_TYPE_OPTIONS = [
  {
    id: 'On demand',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.triggerTypeOptions.onDemand',
      { defaultMessage: 'On demand' }
    ),
  },
  {
    id: 'Schedule',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.triggerTypeOptions.schedule',
      { defaultMessage: 'Schedule' }
    ),
  },
];

export const SCHEDULE_TYPE_OPTIONS = [
  {
    id: 'Recurring',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.scheduleTypeOptions.recurring',
      { defaultMessage: 'Recurring' }
    ),
  },
  {
    id: 'Cron based',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.scheduleTypeOptions.cronBased',
      { defaultMessage: 'Cron based' }
    ),
  },
];

export const SCHEDULE_RECURRING_OPTIONS = [
  {
    value: 'daily',
    text: i18n.translate(
      'opensearch.reports.reportTriggerConstants.scheduleRecurringOptions.daily',
      { defaultMessage: 'Daily' }
    ),
  },
  {
    value: 'byInterval',
    text: i18n.translate(
      'opensearch.reports.reportTriggerConstants.scheduleRecurringOptions.byInterval',
      { defaultMessage: 'By interval' }
    ),
  },
  // TODO: disable on UI. Add them back once we support
  //   {
  //     value: 'weekly',
  //     text: 'Weekly',
  //   },
  //   {
  //     value: 'monthly',
  //     text: 'Monthly',
  //   },
];

export const INTERVAL_TIME_PERIODS = [
  {
    value: 'MINUTES',
    text: i18n.translate(
      'opensearch.reports.reportTriggerConstants.intervalTimePeriods.minutes',
      { defaultMessage: 'Minutes' }
    ),
  },
  {
    value: 'HOURS',
    text: i18n.translate(
      'opensearch.reports.reportTriggerConstants.intervalTimePeriods.hours',
      { defaultMessage: 'Hours' }
    ),
  },
  {
    value: 'DAYS',
    text: i18n.translate(
      'opensearch.reports.reportTriggerConstants.intervalTimePeriods.days',
      { defaultMessage: 'Days' }
    ),
  },
];

export const WEEKLY_CHECKBOX_OPTIONS = [
  {
    id: 'monCheckbox',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.weeklyCheckboxOptions.mon',
      { defaultMessage: 'Mon' }
    ),
  },
  {
    id: 'tueCheckbox',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.weeklyCheckboxOptions.tue',
      { defaultMessage: 'Tue' }
    ),
  },
  {
    id: 'wedCheckbox',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.weeklyCheckboxOptions.wed',
      { defaultMessage: 'Wed' }
    ),
  },
  {
    id: 'thuCheckbox',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.weeklyCheckboxOptions.thu',
      { defaultMessage: 'Thu' }
    ),
  },
  {
    id: 'friCheckbox',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.weeklyCheckboxOptions.fri',
      { defaultMessage: 'Fri' }
    ),
  },
  {
    id: 'satCheckbox',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.weeklyCheckboxOptions.sat',
      { defaultMessage: 'Sat' }
    ),
  },
  {
    id: 'sunCheckbox',
    label: i18n.translate(
      'opensearch.reports.reportTriggerConstants.weeklyCheckboxOptions.sun',
      { defaultMessage: 'Sun' }
    ),
  },
];

export const MONTHLY_ON_THE_OPTIONS = [
  {
    value: 'day',
    text: i18n.translate(
      'opensearch.reports.reportTriggerConstants.monthlyOnTheOptions.day',
      { defaultMessage: 'Day' }
    ),
  },
];

export const TIMEZONE_OPTIONS = moment.tz
  .names()
  .map((tz) => ({ value: tz, text: tz }));
