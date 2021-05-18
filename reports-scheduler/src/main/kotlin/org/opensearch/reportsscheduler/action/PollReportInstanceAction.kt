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
import org.opensearch.reportsscheduler.model.PollReportInstanceRequest
import org.opensearch.reportsscheduler.model.PollReportInstanceResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * Poll for report instance job transport action
 */
internal class PollReportInstanceAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<PollReportInstanceRequest, PollReportInstanceResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::PollReportInstanceRequest) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/reports/instance/poll"
        internal val ACTION_TYPE = ActionType(NAME, ::PollReportInstanceResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: PollReportInstanceRequest, user: User?): PollReportInstanceResponse {
        return ReportInstanceActions.poll(user)
    }
}
