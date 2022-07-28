/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusRequest
import org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * Update ReportInstance Status transport action
 */
internal class UpdateReportInstanceStatusAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<UpdateReportInstanceStatusRequest, UpdateReportInstanceStatusResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::UpdateReportInstanceStatusRequest) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/instance/update_status"
        internal val ACTION_TYPE = ActionType(NAME, ::UpdateReportInstanceStatusResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: UpdateReportInstanceStatusRequest, user: User?): UpdateReportInstanceStatusResponse {
        return ReportInstanceActions.update(request, user)
    }
}
