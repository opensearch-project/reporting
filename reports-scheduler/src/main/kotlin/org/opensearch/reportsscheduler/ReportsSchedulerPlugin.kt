/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler

import org.opensearch.jobscheduler.spi.JobSchedulerExtension
import org.opensearch.jobscheduler.spi.ScheduledJobParser
import org.opensearch.jobscheduler.spi.ScheduledJobRunner
import org.opensearch.reportsscheduler.action.CreateReportDefinitionAction
import org.opensearch.reportsscheduler.action.DeleteReportDefinitionAction
import org.opensearch.reportsscheduler.action.GetAllReportDefinitionsAction
import org.opensearch.reportsscheduler.action.GetAllReportInstancesAction
import org.opensearch.reportsscheduler.action.GetReportDefinitionAction
import org.opensearch.reportsscheduler.action.GetReportInstanceAction
import org.opensearch.reportsscheduler.action.InContextReportCreateAction
import org.opensearch.reportsscheduler.action.OnDemandReportCreateAction
import org.opensearch.reportsscheduler.action.UpdateReportDefinitionAction
import org.opensearch.reportsscheduler.action.UpdateReportInstanceStatusAction
import org.opensearch.reportsscheduler.index.ReportDefinitionsIndex
import org.opensearch.reportsscheduler.index.ReportDefinitionsIndex.REPORT_DEFINITIONS_INDEX_NAME
import org.opensearch.reportsscheduler.index.ReportInstancesIndex
import org.opensearch.reportsscheduler.resthandler.OnDemandReportRestHandler
import org.opensearch.reportsscheduler.resthandler.ReportDefinitionListRestHandler
import org.opensearch.reportsscheduler.resthandler.ReportDefinitionRestHandler
import org.opensearch.reportsscheduler.resthandler.ReportInstanceListRestHandler
import org.opensearch.reportsscheduler.resthandler.ReportInstanceRestHandler
import org.opensearch.reportsscheduler.resthandler.ReportStatsRestHandler
import org.opensearch.reportsscheduler.scheduler.ReportDefinitionJobParser
import org.opensearch.reportsscheduler.scheduler.ReportDefinitionJobRunner
import org.opensearch.reportsscheduler.settings.PluginSettings

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionResponse
import org.opensearch.client.Client
import org.opensearch.cluster.metadata.IndexNameExpressionResolver
import org.opensearch.cluster.node.DiscoveryNodes
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.IndexScopedSettings
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Settings
import org.opensearch.common.settings.SettingsFilter
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.plugins.ActionPlugin
import org.opensearch.plugins.Plugin
import org.opensearch.repositories.RepositoriesService
import org.opensearch.rest.RestController
import org.opensearch.rest.RestHandler
import org.opensearch.script.ScriptService
import org.opensearch.threadpool.ThreadPool
import org.opensearch.watcher.ResourceWatcherService
import java.util.function.Supplier

/**
 * Entry point of the OpenSearch Reports scheduler plugin.
 * This class initializes the rest handlers.
 */
class ReportsSchedulerPlugin : Plugin(), ActionPlugin, JobSchedulerExtension {

    companion object {
        const val PLUGIN_NAME = "opensearch-reports-scheduler"
        const val LOG_PREFIX = "reports"
        const val BASE_REPORTS_URI = "/_plugins/_reports"
        const val LEGACY_BASE_REPORTS_URI = "/_opendistro/_reports"
    }

    /**
     * {@inheritDoc}
     */
    override fun getSettings(): List<Setting<*>> {
        val settingList = arrayListOf<Setting<*>>()
        settingList.addAll(PluginSettings.getAllSettings())
        return settingList
    }

    /**
     * {@inheritDoc}
     */
    override fun createComponents(
        client: Client,
        clusterService: ClusterService,
        threadPool: ThreadPool,
        resourceWatcherService: ResourceWatcherService,
        scriptService: ScriptService,
        xContentRegistry: NamedXContentRegistry,
        environment: Environment,
        nodeEnvironment: NodeEnvironment,
        namedWriteableRegistry: NamedWriteableRegistry,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        repositoriesServiceSupplier: Supplier<RepositoriesService>
    ): Collection<Any> {
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        ReportDefinitionsIndex.initialize(client, clusterService)
        ReportInstancesIndex.initialize(client, clusterService)
        return emptyList()
    }

    /**
     * {@inheritDoc}
     */
    override fun getJobType(): String {
        return "reports-scheduler"
    }

    /**
     * {@inheritDoc}
     */
    override fun getJobIndex(): String {
        return REPORT_DEFINITIONS_INDEX_NAME
    }

    /**
     * {@inheritDoc}
     */
    override fun getJobRunner(): ScheduledJobRunner {
        return ReportDefinitionJobRunner
    }

    /**
     * {@inheritDoc}
     */
    override fun getJobParser(): ScheduledJobParser {
        return ReportDefinitionJobParser
    }

    /**
     * {@inheritDoc}
     */
    override fun getRestHandlers(
        settings: Settings,
        restController: RestController,
        clusterSettings: ClusterSettings,
        indexScopedSettings: IndexScopedSettings,
        settingsFilter: SettingsFilter,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        nodesInCluster: Supplier<DiscoveryNodes>
    ): List<RestHandler> {
        return listOf(
            ReportDefinitionRestHandler(),
            ReportDefinitionListRestHandler(),
            ReportInstanceRestHandler(),
            ReportInstanceListRestHandler(),
            OnDemandReportRestHandler(),
            ReportStatsRestHandler()
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun getActions(): List<ActionPlugin.ActionHandler<out ActionRequest, out ActionResponse>> {
        return listOf(
            ActionPlugin.ActionHandler(CreateReportDefinitionAction.ACTION_TYPE, CreateReportDefinitionAction::class.java),
            ActionPlugin.ActionHandler(DeleteReportDefinitionAction.ACTION_TYPE, DeleteReportDefinitionAction::class.java),
            ActionPlugin.ActionHandler(GetAllReportDefinitionsAction.ACTION_TYPE, GetAllReportDefinitionsAction::class.java),
            ActionPlugin.ActionHandler(GetAllReportInstancesAction.ACTION_TYPE, GetAllReportInstancesAction::class.java),
            ActionPlugin.ActionHandler(GetReportDefinitionAction.ACTION_TYPE, GetReportDefinitionAction::class.java),
            ActionPlugin.ActionHandler(GetReportInstanceAction.ACTION_TYPE, GetReportInstanceAction::class.java),
            ActionPlugin.ActionHandler(InContextReportCreateAction.ACTION_TYPE, InContextReportCreateAction::class.java),
            ActionPlugin.ActionHandler(OnDemandReportCreateAction.ACTION_TYPE, OnDemandReportCreateAction::class.java),
            ActionPlugin.ActionHandler(UpdateReportDefinitionAction.ACTION_TYPE, UpdateReportDefinitionAction::class.java),
            ActionPlugin.ActionHandler(UpdateReportInstanceStatusAction.ACTION_TYPE, UpdateReportInstanceStatusAction::class.java)
        )
    }
}
