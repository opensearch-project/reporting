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
import org.opensearch.reportsscheduler.model.UpdateReportDefinitionRequest
import org.opensearch.reportsscheduler.model.UpdateReportDefinitionResponse
import org.opensearch.transport.TransportService

/**
 * Update reportDefinitions transport action
 */
internal class UpdateReportDefinitionAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<UpdateReportDefinitionRequest, UpdateReportDefinitionResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::UpdateReportDefinitionRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/definition/update"
        internal val ACTION_TYPE = ActionType(NAME, ::UpdateReportDefinitionResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: UpdateReportDefinitionRequest, user: User?): UpdateReportDefinitionResponse {
        return ReportDefinitionActions.update(request, user)
    }
}
