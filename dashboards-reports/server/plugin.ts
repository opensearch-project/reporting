/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
import opensearchReportsPlugin from './backend/opensearch-reports-plugin';
import {
  ReportsDashboardsPluginSetup,
  ReportsDashboardsPluginStart,
} from './types';
import registerRoutes from './routes';
import { pollAndExecuteJob } from './executor/executor';
import { POLL_INTERVAL } from './utils/constants';
import { NotificationsPlugin } from './clusters/notificationsPlugin';
import { buildConfig, ReportingConfigType } from './config';
import { ReportingConfig } from './config/config';

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
  private readonly initializerContext: PluginInitializerContext<
    ReportingConfigType
  >;
  private reportingConfig?: ReportingConfig;

  constructor(context: PluginInitializerContext<ReportingConfigType>) {
    this.logger = context.logger.get();
    this.initializerContext = context;
    const timeoutError = new Error('Server busy');
    timeoutError.statusCode = 503;
    this.semaphore = withTimeout(new Semaphore(1), 180000, timeoutError);
  }

  public async setup(core: CoreSetup) {
    this.logger.debug('reports-dashboards: Setup');

    try {
      const config = await buildConfig(
        this.initializerContext,
        core,
        this.logger
      );
      this.reportingConfig = config;
      this.logger.debug('Setup complete');
    } catch (error) {
      this.logger.error(
        `Error in Reporting setup, reporting may not function properly`
      );
      this.logger.error(error);
    }

    if (!this.reportingConfig) {
      throw new Error('Reporting Config is not initialized');
    }

    const router = core.http.createRouter();
    // Deprecated API. Switch to the new opensearch client as soon as https://github.com/elastic/kibana/issues/35508 done.
    const opensearchReportsClient: ILegacyClusterClient = core.opensearch.legacy.createClient(
      'opensearch_reports',
      {
        plugins: [opensearchReportsPlugin, NotificationsPlugin],
      }
    );

    const notificationsClient: ILegacyClusterClient = core.opensearch.legacy.createClient(
      'opensearch_notifications',
      {
        plugins: [NotificationsPlugin],
      }
    );

    // Register server side APIs
    registerRoutes(router, this.reportingConfig);

    // put logger into route handler context, so that we don't need to pass through parameters
    core.http.registerRouteHandlerContext(
      //@ts-ignore
      'reporting_plugin',
      (context, request) => {
        return {
          logger: this.logger,
          semaphore: this.semaphore,
          opensearchReportsClient,
          notificationsClient,
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
