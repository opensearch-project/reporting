/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.GetAllReportDefinitionsRequest
import org.opensearch.reportsscheduler.model.GetAllReportDefinitionsResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * Get all reportDefinitions transport action
 */
internal class GetAllReportDefinitionsAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetAllReportDefinitionsRequest, GetAllReportDefinitionsResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::GetAllReportDefinitionsRequest) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/definition/list"
        internal val ACTION_TYPE = ActionType(NAME, ::GetAllReportDefinitionsResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: GetAllReportDefinitionsRequest, user: User?): GetAllReportDefinitionsResponse {
        return ReportDefinitionActions.getAll(request, user)
    }
}
