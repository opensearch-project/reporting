/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import { IUiSettingsClient } from '../../../../../src/core/public';

let uiSettings: IUiSettingsClient;

export const uiSettingsService = {
  init: (client: IUiSettingsClient) => {
    uiSettings = client;
  },
  get: (key: string, defaultOverride?: any) => {
    return uiSettings?.get(key, defaultOverride) || '';
  },
  getSearchParams: function () {
    const rawTimeZone = this.get('dateFormat:tz');
    const timezone =
      !rawTimeZone || rawTimeZone === 'Browser'
        ? Intl.DateTimeFormat().resolvedOptions().timeZone
        : rawTimeZone;
    const dateFormat = this.get('dateFormat');
    const csvSeparator = this.get('csv:separator');
    const allowLeadingWildcards = this.get('query:allowLeadingWildcards');
    return {
      timezone,
      dateFormat,
      csvSeparator,
      allowLeadingWildcards,
    };
  },
};
