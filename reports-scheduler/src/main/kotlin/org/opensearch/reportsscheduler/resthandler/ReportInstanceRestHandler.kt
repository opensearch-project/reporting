/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.reportsscheduler.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.reportsscheduler.action.GetReportInstanceAction
import org.opensearch.reportsscheduler.action.ReportInstanceActions
import org.opensearch.reportsscheduler.action.UpdateReportInstanceStatusAction
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.GetReportInstanceRequest
import org.opensearch.reportsscheduler.model.RestTag.REPORT_INSTANCE_ID_FIELD
import org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusRequest
import org.opensearch.reportsscheduler.util.contentParserNextToken
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.ReplacedRoute
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestStatus

/**
 * Rest handler for report instances lifecycle management.
 * This handler uses [ReportInstanceActions].
 */
internal class ReportInstanceRestHandler : PluginBaseHandler() {
    companion object {
        private const val REPORT_INSTANCE_LIST_ACTION = "report_instance_actions"
        private const val REPORT_INSTANCE_URL = "$BASE_REPORTS_URI/instance"
        private const val LEGACY_REPORT_INSTANCE_URL = "$LEGACY_BASE_REPORTS_URI/instance"
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
             * Update report instance status
             * Request URL: POST REPORT_INSTANCE_URL/{reportInstanceId}
             * Request body: Ref [org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusRequest]
             * Response body: Ref [org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusResponse]
             */
            ReplacedRoute(
                POST,
                "$REPORT_INSTANCE_URL/{$REPORT_INSTANCE_ID_FIELD}",
                POST,
                "$LEGACY_REPORT_INSTANCE_URL/{$REPORT_INSTANCE_ID_FIELD}"
            ),
            /**
             * Get a report instance information
             * Request URL: GET REPORT_INSTANCE_URL/{reportInstanceId}
             * Request body: None
             * Response body: Ref [org.opensearch.reportsscheduler.model.GetReportInstanceResponse]
             */
            ReplacedRoute(
                GET,
                "$REPORT_INSTANCE_URL/{$REPORT_INSTANCE_ID_FIELD}",
                GET,
                "$LEGACY_REPORT_INSTANCE_URL/{$REPORT_INSTANCE_ID_FIELD}"
            )
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(REPORT_INSTANCE_ID_FIELD)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        val reportInstanceId = request.param(REPORT_INSTANCE_ID_FIELD) ?: throw IllegalArgumentException("Must specify id")
        return when (request.method()) {
            POST -> RestChannelConsumer {
                Metrics.REPORT_INSTANCE_UPDATE_TOTAL.counter.increment()
                Metrics.REPORT_INSTANCE_UPDATE_INTERVAL_COUNT.counter.increment()
                client.execute(
                    UpdateReportInstanceStatusAction.ACTION_TYPE,
                    UpdateReportInstanceStatusRequest.parse(request.contentParserNextToken(), reportInstanceId),
                    RestResponseToXContentListener(it)
                )
            }
            GET -> RestChannelConsumer {
                Metrics.REPORT_INSTANCE_INFO_TOTAL.counter.increment()
                Metrics.REPORT_INSTANCE_INFO_INTERVAL_COUNT.counter.increment()
                client.execute(
                    GetReportInstanceAction.ACTION_TYPE,
                    GetReportInstanceRequest(reportInstanceId),
                    RestResponseToXContentListener(it)
                )
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }
}
