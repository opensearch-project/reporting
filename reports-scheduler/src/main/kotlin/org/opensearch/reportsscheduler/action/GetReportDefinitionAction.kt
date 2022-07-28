/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.GetReportDefinitionRequest
import org.opensearch.reportsscheduler.model.GetReportDefinitionResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * Get reportDefinition transport action
 */
internal class GetReportDefinitionAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetReportDefinitionRequest, GetReportDefinitionResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::GetReportDefinitionRequest) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/definition/get"
        internal val ACTION_TYPE = ActionType(NAME, ::GetReportDefinitionResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: GetReportDefinitionRequest, user: User?): GetReportDefinitionResponse {
        return ReportDefinitionActions.info(request, user)
    }
}
