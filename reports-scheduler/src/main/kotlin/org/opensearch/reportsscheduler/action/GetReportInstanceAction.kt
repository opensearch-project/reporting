/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.GetReportInstanceRequest
import org.opensearch.reportsscheduler.model.GetReportInstanceResponse
import org.opensearch.transport.TransportService

/**
 * Get report instance transport action
 */
internal class GetReportInstanceAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetReportInstanceRequest, GetReportInstanceResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::GetReportInstanceRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/instance/get"
        internal val ACTION_TYPE = ActionType(NAME, ::GetReportInstanceResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: GetReportInstanceRequest, user: User?): GetReportInstanceResponse {
        return ReportInstanceActions.info(request, user)
    }
}
