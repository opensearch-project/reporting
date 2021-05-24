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
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.client.node.NodeClient
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestHandler.ReplacedRoute
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestStatus

/**
 * Rest handler for getting reporting backend stats
 */
internal class ReportStatsRestHandler : BaseRestHandler() {
    companion object {
        private const val REPORT_STATS_ACTION = "report_definition_stats"
        private const val REPORT_STATS_URL = "$BASE_REPORTS_URI/_local/stats"
        private const val LEGACY_REPORT_STATS_URL = "$LEGACY_BASE_REPORTS_URI/_local/stats"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return REPORT_STATS_ACTION
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
             * Get reporting backend stats
             * Request URL: GET REPORT_STATS_URL
             * Response body derived from: Ref [org.opensearch.reportsscheduler.metrics.Metrics]
             */
            ReplacedRoute(
                GET,
                "$REPORT_STATS_URL",
                GET,
                "$LEGACY_REPORT_STATS_URL"
            )
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf()
    }

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            // TODO: Wrap this into TransportAction
            GET -> RestChannelConsumer {
                // it.sendResponse(BytesRestResponse(RestStatus.OK, Metrics.getInstance().collectToFlattenedJSON()))
                it.sendResponse(BytesRestResponse(RestStatus.OK, Metrics.collectToFlattenedJSON()))
            }
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }
}
