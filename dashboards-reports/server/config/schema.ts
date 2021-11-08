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

import { schema, TypeOf } from '@osd/config-schema';

const OsdServerSchema = schema.object({
  hostname: schema.maybe(
    schema.string({
      validate(value) {
        if (value === '0') {
          return 'must not be "0" for the headless browser to correctly resolve the host';
        }
      },
      hostname: true,
    })
  ),
  port: schema.maybe(schema.number()),
  protocol: schema.maybe(
    schema.string({
      validate(value) {
        if (!/^https?$/.test(value)) {
          return 'must be "http" or "https"';
        }
      },
    })
  ),
}); // default values are all dynamic in createConfig$

export const ConfigSchema = schema.object({
  osd_server: OsdServerSchema,
});

export type ReportingConfigType = TypeOf<typeof ConfigSchema>;
