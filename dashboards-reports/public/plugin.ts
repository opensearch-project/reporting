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
  AppMountParameters,
  CoreSetup,
  CoreStart,
  Plugin,
} from '../../../src/core/public';
import {
  ReportsDashboardsPluginSetup,
  ReportsDashboardsPluginStart,
  AppPluginStartDependencies,
} from './types';
import { i18n } from '@osd/i18n';
import './components/context_menu/context_menu';
import { PLUGIN_ID, PLUGIN_NAME } from '../common';
import { uiSettingsService } from './components/utils/settings_service';

export class ReportsDashboardsPlugin
  implements Plugin<ReportsDashboardsPluginSetup, ReportsDashboardsPluginStart>
{
  public setup(core: CoreSetup): ReportsDashboardsPluginSetup {
    uiSettingsService.init(core.uiSettings, core.http);
    // Register an application into the side navigation menu
    core.application.register({
      id: PLUGIN_ID,
      title: i18n.translate('opensearch.reports.pluginName', {
        defaultMessage: PLUGIN_NAME,
      }),
      category: {
        id: 'opensearch',
        label: i18n.translate('opensearch.reports.categoryName', {
          defaultMessage: 'OpenSearch Plugins',
        }),
        order: 2000,
      },
      order: 2000,
      async mount(params: AppMountParameters) {
        // Load application bundle
        const { renderApp } = await import('./application');
        // Get start services as specified in opensearch_dashboards.json
        const [coreStart, depsStart] = await core.getStartServices();
        // Render the application
        return renderApp(
          coreStart,
          depsStart as AppPluginStartDependencies,
          params
        );
      },
    });

    // Return methods that should be available to other plugins
    return {};
  }

  public start(core: CoreStart): ReportsDashboardsPluginStart {
    return {};
  }

  public stop() {}
}
