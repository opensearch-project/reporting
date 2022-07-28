/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
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
    uiSettingsService.init(core.uiSettings);
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
