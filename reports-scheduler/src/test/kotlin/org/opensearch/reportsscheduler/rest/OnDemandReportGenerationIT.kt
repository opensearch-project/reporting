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

package org.opensearch.reportsscheduler.rest

import org.opensearch.reportsscheduler.PluginRestTestCase
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.reportsscheduler.jsonify
import org.opensearch.reportsscheduler.validateErrorResponse
import org.opensearch.reportsscheduler.validateTimeNearRefTime
import org.opensearch.reportsscheduler.validateTimeRecency
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.junit.Assert
import java.time.Duration
import java.time.Instant

class OnDemandReportGenerationIT : PluginRestTestCase() {
    fun `test create on-demand report from definition`() {
        val reportDefinitionRequest = """
            {
                "reportDefinition":{
                    "name":"report_definition",
                    "isEnabled":true,
                    "source":{
                        "description":"description",
                        "type":"Dashboard",
                        "origin":"localhost:5601",
                        "id":"id"
                    },
                    "format":{
                        "duration":"PT1H",
                        "fileFormat":"Pdf",
                        "limit":1000,
                        "header":"optional header",
                        "footer":"optional footer"
                    },
                    "trigger":{
                        "triggerType":"OnDemand"
                    }
                }
            }
        """.trimIndent()
        val reportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = reportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)
        val onDemandRequest = """
            {}
        """.trimIndent()
        val onDemandResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/on_demand/$reportDefinitionId",
            onDemandRequest,
            RestStatus.OK.status
        )
        Assert.assertNotNull("reportInstance should be generated", onDemandResponse)
        val reportInstance = onDemandResponse.get("reportInstance").asJsonObject
        val reportDefinitionDetails = reportInstance.get("reportDefinitionDetails").asJsonObject
        val reportDefinition = reportDefinitionDetails.get("reportDefinition").asJsonObject
        Assert.assertEquals(
            reportDefinitionId,
            reportDefinitionDetails.get("id").asString
        )
        Assert.assertEquals(
            jsonify(reportDefinitionRequest)
                .get("reportDefinition").asJsonObject,
            reportDefinition
        )
        validateTimeRecency(Instant.ofEpochMilli(reportInstance.get("lastUpdatedTimeMs").asLong))
        validateTimeRecency(Instant.ofEpochMilli(reportInstance.get("createdTimeMs").asLong))
        validateTimeRecency(Instant.ofEpochMilli(reportInstance.get("endTimeMs").asLong))
        validateTimeNearRefTime(
            Instant.ofEpochMilli(reportInstance.get("beginTimeMs").asLong),
            Instant.now().minus(Duration.parse(reportDefinition.get("format").asJsonObject.get("duration").asString)),
            1
        )
        Assert.assertEquals(
            reportInstance.get("tenant").asString,
            reportDefinitionDetails.get("tenant").asString
        )
        Assert.assertEquals(reportInstance.get("status").asString, "Success")
        Assert.assertNull(reportInstance.get("statusText"))
        Assert.assertNull(reportInstance.get("inContextDownloadUrlPath"))

        // legacy test
        val legacyReportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val legacyReportDefinitionId = legacyReportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", legacyReportDefinitionId)
        Thread.sleep(100)
        val legacyOnDemandRequest = """
            {}
        """.trimIndent()
        val legacyOnDemandResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand/$legacyReportDefinitionId",
            legacyOnDemandRequest,
            RestStatus.OK.status
        )

        Assert.assertNotNull("reportInstance should be generated", legacyOnDemandResponse)
        val legacyReportInstance = legacyOnDemandResponse.get("reportInstance").asJsonObject
        val legacyReportDefinitionDetails = legacyReportInstance.get("reportDefinitionDetails").asJsonObject
        val legacyReportDefinition = legacyReportDefinitionDetails.get("reportDefinition").asJsonObject
        Assert.assertEquals(
            legacyReportDefinitionId,
            legacyReportDefinitionDetails.get("id").asString
        )
        Assert.assertEquals(
            jsonify(reportDefinitionRequest)
                .get("reportDefinition").asJsonObject,
            legacyReportDefinition
        )
        validateTimeRecency(Instant.ofEpochMilli(legacyReportInstance.get("lastUpdatedTimeMs").asLong))
        validateTimeRecency(Instant.ofEpochMilli(legacyReportInstance.get("createdTimeMs").asLong))
        validateTimeRecency(Instant.ofEpochMilli(legacyReportInstance.get("endTimeMs").asLong))
        validateTimeNearRefTime(
            Instant.ofEpochMilli(legacyReportInstance.get("beginTimeMs").asLong),
            Instant.now().minus(Duration.parse(reportDefinition.get("format").asJsonObject.get("duration").asString)),
            1
        )
        Assert.assertEquals(
            legacyReportInstance.get("tenant").asString,
            reportDefinitionDetails.get("tenant").asString
        )
        Assert.assertEquals(legacyReportInstance.get("status").asString, "Success")
        Assert.assertNull(legacyReportInstance.get("statusText"))
        Assert.assertNull(legacyReportInstance.get("inContextDownloadUrlPath"))
    }

    fun `test create on-demand report from invalid definition id`() {
        val reportDefinitionRequest = """
            {
                "reportDefinition":{
                    "name":"report_definition",
                    "isEnabled":true,
                    "source":{
                        "description":"description",
                        "type":"Dashboard",
                        "origin":"localhost:5601",
                        "id":"id"
                    },
                    "format":{
                        "duration":"PT1H",
                        "fileFormat":"Pdf",
                        "limit":1000,
                        "header":"optional header",
                        "footer":"optional footer"
                    },
                    "trigger":{
                        "triggerType":"OnDemand"
                    }
                }
            }
        """.trimIndent()
        val reportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = reportDefinitionResponse.get("reportDefinitionId").asString + "invalid"
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)
        val onDemandRequest = """
            {}
        """.trimIndent()
        val onDemandResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/on_demand/$reportDefinitionId",
            onDemandRequest,
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(onDemandResponse, RestStatus.NOT_FOUND.status)

        // legacy test
        val legacyReportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val legacyReportDefinitionId = legacyReportDefinitionResponse.get("reportDefinitionId").asString + "invalid"
        Assert.assertNotNull("reportDefinitionId should be generated", legacyReportDefinitionId)
        Thread.sleep(100)
        val legacyOnDemandRequest = """
            {}
        """.trimIndent()
        val legacyOnDemandResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand/$legacyReportDefinitionId",
            legacyOnDemandRequest,
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(legacyOnDemandResponse, RestStatus.NOT_FOUND.status)
    }
}
