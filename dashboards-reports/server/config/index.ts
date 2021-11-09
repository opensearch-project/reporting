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

import { PluginConfigDescriptor } from '../../../../src/core/server';
import { ConfigSchema, ReportingConfigType } from './schema';
export { buildConfig } from './config';
export { ConfigSchema, ReportingConfigType };

export const config: PluginConfigDescriptor<ReportingConfigType> = {
  schema: ConfigSchema,
};
