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
    return {
      timezone,
      dateFormat,
      csvSeparator,
    };
  },
};
