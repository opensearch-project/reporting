/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.OnDemandReportCreateRequest
import org.opensearch.reportsscheduler.model.OnDemandReportCreateResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * On-Demand ReportCreate transport action
 */
internal class OnDemandReportCreateAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<OnDemandReportCreateRequest, OnDemandReportCreateResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::OnDemandReportCreateRequest) {
    companion object {
        private const val NAME = "cluster:admin/opendistro/reports/definition/on_demand"
        internal val ACTION_TYPE = ActionType(NAME, ::OnDemandReportCreateResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: OnDemandReportCreateRequest, user: User?): OnDemandReportCreateResponse {
        return ReportInstanceActions.createOnDemandFromDefinition(request, user)
    }
}
