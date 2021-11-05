/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
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
