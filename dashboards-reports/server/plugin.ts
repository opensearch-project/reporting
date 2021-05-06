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

import {
  PluginInitializerContext,
  CoreSetup,
  CoreStart,
  Plugin,
  Logger,
  ILegacyClusterClient,
} from '../../../src/core/server';
import { setIntervalAsync } from 'set-interval-async/dynamic';
import { Semaphore, SemaphoreInterface, withTimeout } from 'async-mutex';
import opensearchReportsPlugin from './backend/opendistro-opensearch-reports-plugin';
import notificationPlugin from './backend/opendistro-notification-plugin';
import {
  ReportsDashboardsPluginSetup,
  ReportsDashboardsPluginStart,
} from './types';
import registerRoutes from './routes';
import { pollAndExecuteJob } from './executor/executor';
import { POLL_INTERVAL } from './utils/constants';

export interface ReportsPluginRequestContext {
  logger: Logger;
  opensearchClient: ILegacyClusterClient;
}
//@ts-ignore
declare module 'kibana/server' {
  interface RequestHandlerContext {
    reports_plugin: ReportsPluginRequestContext;
  }
}

export class ReportsDashboardsPlugin
  implements
    Plugin<ReportsDashboardsPluginSetup, ReportsDashboardsPluginStart> {
  private readonly logger: Logger;
  private readonly semaphore: SemaphoreInterface;

  constructor(initializerContext: PluginInitializerContext) {
    this.logger = initializerContext.logger.get();

    const timeoutError = new Error('Server busy');
    timeoutError.statusCode = 503;
    this.semaphore = withTimeout(new Semaphore(1), 180000, timeoutError);
  }

  public setup(core: CoreSetup) {
    this.logger.debug('reports-dashboards: Setup');
    const router = core.http.createRouter();
    // Deprecated API. Switch to the new opensearch client as soon as https://github.com/elastic/kibana/issues/35508 done.
    const opensearchReportsClient: ILegacyClusterClient = core.opensearch.legacy.createClient(
      'opensearch_reports',
      {
        plugins: [opensearchReportsPlugin],
      }
    );

    const notificationClient: ILegacyClusterClient = core.opensearch.legacy.createClient(
      'notification',
      {
        plugins: [notificationPlugin],
      }
    );

    // Register server side APIs
    registerRoutes(router);

    // put logger into route handler context, so that we don't need to pass through parameters
    core.http.registerRouteHandlerContext(
      //@ts-ignore
      'reporting_plugin',
      (context, request) => {
        return {
          logger: this.logger,
          semaphore: this.semaphore,
          notificationClient,
          opensearchReportsClient,
        };
      }
    );

    return {};
  }

  public start(core: CoreStart) {
    this.logger.debug('reports-dashboards: Started');
    const opensearchReportsClient: ILegacyClusterClient = core.opensearch.legacy.createClient(
      'opensearch_reports',
      {
        plugins: [opensearchReportsPlugin],
      }
    );

    const notificationClient: ILegacyClusterClient = core.opensearch.legacy.createClient(
      'notification',
      {
        plugins: [notificationPlugin],
      }
    );
    const opensearchClient: ILegacyClusterClient =
      core.opensearch.legacy.client;
    /*
    setIntervalAsync provides the same familiar interface as built-in setInterval for asynchronous functions,
    while preventing multiple executions from overlapping in time.
    Polling at at a 5 min fixed interval
    
    TODO: need further optimization polling with a mix approach of
    random delay and dynamic delay based on the amount of jobs. 
    */
    // setIntervalAsync(
    //   pollAndExecuteJob,
    //   POLL_INTERVAL,
    //   opensearchReportsClient,
    //   notificationClient,
    //   opensearchClient,
    //   this.logger
    // );
    return {};
  }

  public stop() {}
}
