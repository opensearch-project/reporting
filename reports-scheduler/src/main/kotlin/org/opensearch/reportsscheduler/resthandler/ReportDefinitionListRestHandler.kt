/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.reportsscheduler.resthandler

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.reportsscheduler.action.GetAllReportDefinitionsAction
import org.opensearch.reportsscheduler.action.ReportDefinitionActions
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.GetAllReportDefinitionsRequest
import org.opensearch.reportsscheduler.model.RestTag.FROM_INDEX_FIELD
import org.opensearch.reportsscheduler.model.RestTag.MAX_ITEMS_FIELD
import org.opensearch.reportsscheduler.settings.PluginSettings

import org.opensearch.client.node.NodeClient
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestHandler.ReplacedRoute
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestStatus

/**
 * Rest handler for getting list of report definitions.
 * This handler uses [ReportDefinitionActions].
 */
internal class ReportDefinitionListRestHandler : PluginBaseHandler() {
    companion object {
        private const val REPORT_DEFINITION_LIST_ACTION = "report_definition_list_actions"
        private const val LIST_REPORT_DEFINITIONS_URL = "$BASE_REPORTS_URI/definitions"
        private const val LEGACY_LIST_REPORT_DEFINITIONS_URL = "$LEGACY_BASE_REPORTS_URI/definitions"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return REPORT_DEFINITION_LIST_ACTION
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf()
    }

    /**
     * {@inheritDoc}
     */
    override fun replacedRoutes(): List<ReplacedRoute> {
        return listOf(
            /**
             * Get all report definitions (from optional fromIndex)
             * Request URL: GET LIST_REPORT_DEFINITIONS_URL[?[fromIndex=1000]&[maxItems=100]]
             * Request body: None
             * Response body: Ref [org.opensearch.reportsscheduler.model.GetAllReportDefinitionsResponse]
             */
            ReplacedRoute(
                GET,
                LIST_REPORT_DEFINITIONS_URL,
                GET,
                LEGACY_LIST_REPORT_DEFINITIONS_URL
            )
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        val from = request.param(FROM_INDEX_FIELD)?.toIntOrNull() ?: 0
        val maxItems = request.param(MAX_ITEMS_FIELD)?.toIntOrNull() ?: PluginSettings.defaultItemsQueryCount
        return when (request.method()) {
            GET -> RestChannelConsumer {
                Metrics.REPORT_DEFINITION_LIST_TOTAL.counter.increment()
                Metrics.REPORT_DEFINITION_LIST_INTERVAL_COUNT.counter.increment()
                client.execute(GetAllReportDefinitionsAction.ACTION_TYPE,
                    GetAllReportDefinitionsRequest(from, maxItems),
                    RestResponseToXContentListener(it))
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(FROM_INDEX_FIELD, MAX_ITEMS_FIELD)
    }
}
