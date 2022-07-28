/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.DeleteReportDefinitionRequest
import org.opensearch.reportsscheduler.model.DeleteReportDefinitionResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * Delete reportDefinition transport action
 */
internal class DeleteReportDefinitionAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<DeleteReportDefinitionRequest, DeleteReportDefinitionResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::DeleteReportDefinitionRequest) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/definition/delete"
        internal val ACTION_TYPE = ActionType(NAME, ::DeleteReportDefinitionResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: DeleteReportDefinitionRequest, user: User?): DeleteReportDefinitionResponse {
        return ReportDefinitionActions.delete(request, user)
    }
}
