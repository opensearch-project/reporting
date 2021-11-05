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
import org.opensearch.reportsscheduler.model.CreateReportDefinitionRequest
import org.opensearch.reportsscheduler.model.CreateReportDefinitionResponse
import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.transport.TransportService

/**
 * Create reportDefinition transport action
 */
internal class CreateReportDefinitionAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<CreateReportDefinitionRequest, CreateReportDefinitionResponse>(NAME,
    transportService,
    client,
    actionFilters,
    ::CreateReportDefinitionRequest) {
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
