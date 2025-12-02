/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.common.inject.Inject
import org.opensearch.commons.authuser.User
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.reportsscheduler.model.GetAllReportInstancesRequest
import org.opensearch.reportsscheduler.model.GetAllReportInstancesResponse
import org.opensearch.reportsscheduler.util.PluginClient
import org.opensearch.transport.TransportService
import org.opensearch.transport.client.Client

/**
 * Get all report instances transport action
 */
internal class GetAllReportInstancesAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    private val pluginClient: PluginClient,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetAllReportInstancesRequest, GetAllReportInstancesResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::GetAllReportInstancesRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/instance/list"
        internal val ACTION_TYPE = ActionType(NAME, ::GetAllReportInstancesResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: GetAllReportInstancesRequest, user: User?): GetAllReportInstancesResponse {
        return ReportInstanceActions.getAll(request, pluginClient, user)
    }
}
