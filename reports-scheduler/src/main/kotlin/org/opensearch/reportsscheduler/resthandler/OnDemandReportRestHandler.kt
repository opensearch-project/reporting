/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.reportsscheduler.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.reportsscheduler.action.InContextReportCreateAction
import org.opensearch.reportsscheduler.action.OnDemandReportCreateAction
import org.opensearch.reportsscheduler.action.ReportInstanceActions
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.InContextReportCreateRequest
import org.opensearch.reportsscheduler.model.OnDemandReportCreateRequest
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_ID_FIELD
import org.opensearch.reportsscheduler.util.contentParserNextToken
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.ReplacedRoute
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestRequest.Method.PUT
import org.opensearch.rest.RestStatus

/**
 * Rest handler for creating on-demand report instances.
 * This handler uses [ReportInstanceActions].
 */
internal class OnDemandReportRestHandler : PluginBaseHandler() {
    companion object {
        private const val REPORT_INSTANCE_LIST_ACTION = "on_demand_report_actions"
        private const val ON_DEMAND_REPORT_URL = "$BASE_REPORTS_URI/on_demand"
        private const val LEGACY_ON_DEMAND_REPORT_URL = "$LEGACY_BASE_REPORTS_URI/on_demand"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return REPORT_INSTANCE_LIST_ACTION
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
             * Create a new report instance from provided definition
             * Request URL: PUT ON_DEMAND_REPORT_URL
             * Request body: Ref [org.opensearch.reportsscheduler.model.InContextReportCreateRequest]
             * Response body: Ref [org.opensearch.reportsscheduler.model.InContextReportCreateResponse]
             */
            ReplacedRoute(
                PUT,
                ON_DEMAND_REPORT_URL,
                PUT,
                LEGACY_ON_DEMAND_REPORT_URL
            ),

            /**
             * Create a new report from definition and return instance
             * Request URL: POST ON_DEMAND_REPORT_URL/{reportDefinitionId}
             * Request body: Ref [org.opensearch.reportsscheduler.model.OnDemandReportCreateRequest]
             * Response body: Ref [org.opensearch.reportsscheduler.model.OnDemandReportCreateResponse]
             */
            ReplacedRoute(
                POST,
                "$ON_DEMAND_REPORT_URL/{$REPORT_DEFINITION_ID_FIELD}",
                POST,
                "$LEGACY_ON_DEMAND_REPORT_URL/{$REPORT_DEFINITION_ID_FIELD}"
            )
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(REPORT_DEFINITION_ID_FIELD)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            PUT -> RestChannelConsumer {
                Metrics.REPORT_FROM_DEFINITION_TOTAL.counter.increment()
                Metrics.REPORT_FROM_DEFINITION_INTERVAL_COUNT.counter.increment()
                client.execute(
                    InContextReportCreateAction.ACTION_TYPE,
                    InContextReportCreateRequest(request.contentParserNextToken()),
                    RestResponseToXContentListener(it)
                )
            }
            POST -> RestChannelConsumer {
                Metrics.REPORT_FROM_DEFINITION_ID_TOTAL.counter.increment()
                Metrics.REPORT_FROM_DEFINITION_ID_INTERVAL_COUNT.counter.increment()
                client.execute(
                    OnDemandReportCreateAction.ACTION_TYPE,
                    OnDemandReportCreateRequest.parse(request.contentParserNextToken(), request.param(REPORT_DEFINITION_ID_FIELD)),
                    RestResponseToXContentListener(it)
                )
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }
}
