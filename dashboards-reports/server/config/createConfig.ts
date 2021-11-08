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

import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CoreSetup, Logger } from '../../../../src/core/server';

import { ReportingConfigType } from './schema';

/*
 * Set up dynamic config defaults
 */
export function createConfig$(
  core: CoreSetup,
  config$: Observable<ReportingConfigType>,
  logger: Logger
) {
  return config$.pipe(
    map((config) => {
      const { osd_server: reportingServer } = config;
      const serverInfo = core.http.getServerInfo();
      // osd_server.hostname, default to server.host
      const osdServerHostname = reportingServer.hostname
        ? reportingServer.hostname
        : serverInfo.hostname;

      // osd_server.port, default to server.port
      const osdServerPort = reportingServer.port
        ? reportingServer.port
        : serverInfo.port;
      // osd_server.protocol, default to server.protocol
      const osdServerProtocol = reportingServer.protocol
        ? reportingServer.protocol
        : serverInfo.protocol;
      return {
        ...config,
        osd_server: {
          hostname: osdServerHostname,
          port: osdServerPort,
          protocol: osdServerProtocol,
        },
      };
    })
  );
}
