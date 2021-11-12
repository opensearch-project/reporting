/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.InContextReportCreateRequest
import org.opensearch.reportsscheduler.model.InContextReportCreateResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * In-Context ReportCreate transport action
 */
internal class InContextReportCreateAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<InContextReportCreateRequest, InContextReportCreateResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::InContextReportCreateRequest) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/menu/download"
        internal val ACTION_TYPE = ActionType(NAME, ::InContextReportCreateResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: InContextReportCreateRequest, user: User?): InContextReportCreateResponse {
        return ReportInstanceActions.createOnDemand(request, user)
    }
}
