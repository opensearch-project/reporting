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
import org.opensearch.reportsscheduler.model.CreateReportDefinitionRequest
import org.opensearch.reportsscheduler.model.CreateReportDefinitionResponse
import org.opensearch.transport.TransportService

/**
 * Create reportDefinition transport action
 */
internal class CreateReportDefinitionAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<CreateReportDefinitionRequest, CreateReportDefinitionResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::CreateReportDefinitionRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/definition/create"
        internal val ACTION_TYPE = ActionType(NAME, ::CreateReportDefinitionResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: CreateReportDefinitionRequest, user: User?): CreateReportDefinitionResponse {
        return ReportDefinitionActions.create(request, user)
    }
}
