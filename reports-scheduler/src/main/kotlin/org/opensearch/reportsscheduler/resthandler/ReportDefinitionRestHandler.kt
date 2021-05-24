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
 *
 */
package org.opensearch.reportsscheduler.resthandler

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.reportsscheduler.action.CreateReportDefinitionAction
import org.opensearch.reportsscheduler.action.DeleteReportDefinitionAction
import org.opensearch.reportsscheduler.action.GetReportDefinitionAction
import org.opensearch.reportsscheduler.action.ReportDefinitionActions
import org.opensearch.reportsscheduler.action.UpdateReportDefinitionAction
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.CreateReportDefinitionRequest
import org.opensearch.reportsscheduler.model.DeleteReportDefinitionRequest
import org.opensearch.reportsscheduler.model.GetReportDefinitionRequest
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_ID_FIELD
import org.opensearch.reportsscheduler.model.UpdateReportDefinitionRequest
import org.opensearch.reportsscheduler.util.contentParserNextToken
import org.opensearch.client.node.NodeClient
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestHandler.ReplacedRoute
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.DELETE
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestRequest.Method.PUT
import org.opensearch.rest.RestStatus

/**
 * Rest handler for report definitions lifecycle management.
 * This handler uses [ReportDefinitionActions].
 */
internal class ReportDefinitionRestHandler : PluginBaseHandler() {
    companion object {
        private const val REPORT_DEFINITION_ACTION = "report_definition_actions"
        private const val REPORT_DEFINITION_URL = "$BASE_REPORTS_URI/definition"
        private const val LEGACY_REPORT_DEFINITION_URL = "$LEGACY_BASE_REPORTS_URI/definition"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return REPORT_DEFINITION_ACTION
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
             * Create a new report definition
             * Request URL: POST REPORT_DEFINITION_URL
             * Request body: Ref [org.opensearch.reportsscheduler.model.CreateReportDefinitionRequest]
             * Response body: Ref [org.opensearch.reportsscheduler.model.CreateReportDefinitionResponse]
             */
            ReplacedRoute(
                POST,
                REPORT_DEFINITION_URL,
                POST,
                LEGACY_REPORT_DEFINITION_URL
            ),
            /**
             * Update report definition
             * Request URL: PUT REPORT_DEFINITION_URL/{reportDefinitionId}
             * Request body: Ref [org.opensearch.reportsscheduler.model.UpdateReportDefinitionRequest]
             * Response body: Ref [org.opensearch.reportsscheduler.model.UpdateReportDefinitionResponse]
             */
            ReplacedRoute(
                PUT,
                "$REPORT_DEFINITION_URL/{$REPORT_DEFINITION_ID_FIELD}",
                PUT,
                "$LEGACY_REPORT_DEFINITION_URL/{$REPORT_DEFINITION_ID_FIELD}"
            ),
            /**
             * Get a report definition
             * Request URL: GET REPORT_DEFINITION_URL/{reportDefinitionId}
             * Request body: Ref [org.opensearch.reportsscheduler.model.GetReportDefinitionRequest]
             * Response body: Ref [org.opensearch.reportsscheduler.model.GetReportDefinitionResponse]
             */
            ReplacedRoute(
                GET,
                "$REPORT_DEFINITION_URL/{$REPORT_DEFINITION_ID_FIELD}",
                GET,
                "$LEGACY_REPORT_DEFINITION_URL/{$REPORT_DEFINITION_ID_FIELD}"
            ),
            /**
             * Delete report definition
             * Request URL: DELETE REPORT_DEFINITION_URL/{reportDefinitionId}
             * Request body: Ref [org.opensearch.reportsscheduler.model.DeleteReportDefinitionRequest]
             * Response body: Ref [org.opensearch.reportsscheduler.model.DeleteReportDefinitionResponse]
             */
            ReplacedRoute(
                DELETE,
                "$REPORT_DEFINITION_URL/{$REPORT_DEFINITION_ID_FIELD}",
                DELETE,
                "$LEGACY_REPORT_DEFINITION_URL/{$REPORT_DEFINITION_ID_FIELD}"
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
            POST -> RestChannelConsumer {
                Metrics.REPORT_DEFINITION_CREATE_TOTAL.counter.increment()
                Metrics.REPORT_DEFINITION_CREATE_INTERVAL_COUNT.counter.increment()
                client.execute(CreateReportDefinitionAction.ACTION_TYPE,
                    CreateReportDefinitionRequest(request.contentParserNextToken()),
                    RestResponseToXContentListener(it))
            }
            PUT -> RestChannelConsumer {
                Metrics.REPORT_DEFINITION_UPDATE_TOTAL.counter.increment()
                Metrics.REPORT_DEFINITION_UPDATE_INTERVAL_COUNT.counter.increment()
                client.execute(
                    UpdateReportDefinitionAction.ACTION_TYPE,
                    UpdateReportDefinitionRequest(request.contentParserNextToken(), request.param(REPORT_DEFINITION_ID_FIELD)),
                    RestResponseToXContentListener(it))
            }
            GET -> RestChannelConsumer {
                Metrics.REPORT_DEFINITION_INFO_TOTAL.counter.increment()
                Metrics.REPORT_DEFINITION_INFO_INTERVAL_COUNT.counter.increment()
                client.execute(GetReportDefinitionAction.ACTION_TYPE,
                    GetReportDefinitionRequest(request.param(REPORT_DEFINITION_ID_FIELD)),
                    RestResponseToXContentListener(it))
            }
            DELETE -> RestChannelConsumer {
                Metrics.REPORT_DEFINITION_DELETE_TOTAL.counter.increment()
                Metrics.REPORT_DEFINITION_DELETE_INTERVAL_COUNT.counter.increment()
                client.execute(DeleteReportDefinitionAction.ACTION_TYPE,
                    DeleteReportDefinitionRequest(request.param(REPORT_DEFINITION_ID_FIELD)),
                    RestResponseToXContentListener(it))
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }
}
