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
import org.opensearch.reportsscheduler.model.GetReportInstanceRequest
import org.opensearch.reportsscheduler.model.GetReportInstanceResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * Get report instance transport action
 */
internal class GetReportInstanceAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetReportInstanceRequest, GetReportInstanceResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::GetReportInstanceRequest) {
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
