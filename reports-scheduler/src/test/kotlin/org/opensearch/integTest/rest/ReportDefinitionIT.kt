/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integTest.rest

import org.opensearch.integTest.PluginRestTestCase
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.integTest.constructReportDefinitionRequest
import org.opensearch.integTest.validateErrorResponse
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.junit.Assert

class ReportDefinitionIT : PluginRestTestCase() {
    fun `test create, get, update, delete report definition`() {
        val reportDefinitionOnDemandRequest = constructReportDefinitionRequest()
        val reportDefinitionOnDemandResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionOnDemandRequest,
            RestStatus.OK.status
        )
        val reportDefinitionOnDemandId = reportDefinitionOnDemandResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionOnDemandId)
        Thread.sleep(100)

        val reportDefinitionDownloadRequest = constructReportDefinitionRequest(
            """
                "trigger":{
                    "triggerType":"Download"
                },
            """.trimIndent()
        )
        val reportDefinitionDownloadResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionDownloadRequest,
            RestStatus.OK.status
        )
        val reportDefinitionDownloadId = reportDefinitionDownloadResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionDownloadId)
        Thread.sleep(1000)

        val reportDefinitionsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_REPORTS_URI/definitions",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(2, reportDefinitionsResponse.get("totalHits").asInt)
        Thread.sleep(100)

        val newName = "updated_report"
        val reportDefinitionUpdateRequest = constructReportDefinitionRequest(name = newName)
        val reportDefinitionUpdateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            reportDefinitionUpdateRequest,
            RestStatus.OK.status
        )
        Assert.assertEquals(
            reportDefinitionOnDemandId,
            reportDefinitionUpdateResponse.get("reportDefinitionId").asString
        )
        Thread.sleep(100)

        val reportDefinitionGetResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(
            reportDefinitionOnDemandId,
            reportDefinitionGetResponse.get("reportDefinitionDetails").asJsonObject.get("id").asString
        )
        Assert.assertEquals(
            newName,
            reportDefinitionGetResponse.get("reportDefinitionDetails").asJsonObject
                .get("reportDefinition").asJsonObject.get("name").asString
        )
        Thread.sleep(100)

        val reportDefinitionDeleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(
            reportDefinitionOnDemandId,
            reportDefinitionDeleteResponse.get("reportDefinitionId").asString
        )
        Thread.sleep(100)

        val reportDefinitionGetNotFoundResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionGetNotFoundResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val reportDefinitionInvalidGetResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_REPORTS_URI/definition/invalid-id",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionInvalidGetResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val reportDefinitionInvalidUpdateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/definition/invalid-id",
            constructReportDefinitionRequest(),
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionInvalidUpdateResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val reportDefinitionInvalidDeleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_REPORTS_URI/definition/invalid-id",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionInvalidDeleteResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)
    }

    fun `test legacy create, get, update, delete report definition`() {
        val reportDefinitionOnDemandRequest = constructReportDefinitionRequest()
        val reportDefinitionOnDemandResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionOnDemandRequest,
            RestStatus.OK.status
        )
        val reportDefinitionOnDemandId = reportDefinitionOnDemandResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionOnDemandId)
        Thread.sleep(100)

        val reportDefinitionDownloadRequest = constructReportDefinitionRequest(
            """
                "trigger":{
                    "triggerType":"Download"
                },
            """.trimIndent()
        )
        val reportDefinitionDownloadResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionDownloadRequest,
            RestStatus.OK.status
        )
        val reportDefinitionDownloadId = reportDefinitionDownloadResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionDownloadId)
        Thread.sleep(1000)

        val reportDefinitionsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$LEGACY_BASE_REPORTS_URI/definitions",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(2, reportDefinitionsResponse.get("totalHits").asInt)
        Thread.sleep(100)

        val newName = "updated_report"
        val reportDefinitionUpdateRequest = constructReportDefinitionRequest(name = newName)
        val reportDefinitionUpdateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            reportDefinitionUpdateRequest,
            RestStatus.OK.status
        )
        Assert.assertEquals(
            reportDefinitionOnDemandId,
            reportDefinitionUpdateResponse.get("reportDefinitionId").asString
        )
        Thread.sleep(100)

        val reportDefinitionGetResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$LEGACY_BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(
            reportDefinitionOnDemandId,
            reportDefinitionGetResponse.get("reportDefinitionDetails").asJsonObject.get("id").asString
        )
        Assert.assertEquals(
            newName,
            reportDefinitionGetResponse.get("reportDefinitionDetails").asJsonObject
                .get("reportDefinition").asJsonObject.get("name").asString
        )
        Thread.sleep(100)

        val reportDefinitionDeleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$LEGACY_BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(
            reportDefinitionOnDemandId,
            reportDefinitionDeleteResponse.get("reportDefinitionId").asString
        )
        Thread.sleep(100)

        val reportDefinitionGetNotFoundResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$LEGACY_BASE_REPORTS_URI/definition/$reportDefinitionOnDemandId",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionGetNotFoundResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val reportDefinitionInvalidGetResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$LEGACY_BASE_REPORTS_URI/definition/invalid-id",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionInvalidGetResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val reportDefinitionInvalidUpdateResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/definition/invalid-id",
            constructReportDefinitionRequest(),
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionInvalidUpdateResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val reportDefinitionInvalidDeleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$LEGACY_BASE_REPORTS_URI/definition/invalid-id",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(reportDefinitionInvalidDeleteResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)
    }

    fun `test create cron scheduled report definition`() {
        val trigger = """
            "trigger":{
                "triggerType":"CronSchedule",
                "schedule":{
                    "cron":{
                        "expression":"0 * * * *",
                        "timezone":"America/Los_Angeles"
                    }
                }
            },
        """.trimIndent()
        val reportDefinitionRequest = constructReportDefinitionRequest(trigger)
        val reportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = reportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)

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
    }

    fun `test create interval scheduled report definition`() {
        val trigger = """
            "trigger":{
                "triggerType":"IntervalSchedule",
                "schedule":{
                    "interval":{
                        "start_time":1603506908773,
                        "period":"10",
                        "unit":"Minutes"
                    }
                }
            },
        """.trimIndent()
        val reportDefinitionRequest = constructReportDefinitionRequest(trigger)
        val reportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = reportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)

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
    }

    fun `test create invalid cron scheduled report definition`() {
        val cronTrigger = """
            "trigger":{
                "triggerType":"CronSchedule"
            },
        """.trimIndent()
        val reportDefinitionCronRequest = constructReportDefinitionRequest(cronTrigger)
        val reportDefinitionCronResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionCronRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(reportDefinitionCronResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
        Thread.sleep(100)

        val invalidCronTrigger = """
            "trigger":{
                "triggerType":"CronSchedule",
                "schedule":{
                    "cron":{
                        "expression":"1234567",
                        "timezone":"America/Los_Angeles"
                    }
                }
            },
        """.trimIndent()
        val reportDefinitionInvalidCronRequest = constructReportDefinitionRequest(invalidCronTrigger)
        val reportDefinitionInvalidCronResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionInvalidCronRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(
            reportDefinitionInvalidCronResponse,
            RestStatus.BAD_REQUEST.status,
            "illegal_argument_exception"
        )
        Thread.sleep(100)

        // legacy test
        val legacyReportDefinitionInvalidCronResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionInvalidCronRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(
            legacyReportDefinitionInvalidCronResponse,
            RestStatus.BAD_REQUEST.status,
            "illegal_argument_exception"
        )
        Thread.sleep(100)
    }

    fun `test create invalid interval scheduled report definition`() {
        val intervalTrigger = """
            "trigger":{
                "triggerType":"IntervalSchedule"
            },
        """.trimIndent()
        val reportDefinitionIntervalRequest = constructReportDefinitionRequest(intervalTrigger)
        val reportDefinitionIntervalResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionIntervalRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(
            reportDefinitionIntervalResponse,
            RestStatus.BAD_REQUEST.status,
            "illegal_argument_exception"
        )
        Thread.sleep(100)

        // legacy test
        val legacyReportDefinitionIntervalResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionIntervalRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(
            legacyReportDefinitionIntervalResponse,
            RestStatus.BAD_REQUEST.status,
            "illegal_argument_exception"
        )
        Thread.sleep(100)

        val invalidIntervalTrigger = """
            "trigger":{
                "triggerType":"IntervalSchedule",
                "schedule":{
                    "interval":{
                    }
                }
            },
        """.trimIndent()
        val reportDefinitionInvalidIntervalRequest = constructReportDefinitionRequest(invalidIntervalTrigger)
        val reportDefinitionInvalidIntervalResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionInvalidIntervalRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(
            reportDefinitionInvalidIntervalResponse,
            RestStatus.BAD_REQUEST.status,
            "illegal_argument_exception"
        )
        Thread.sleep(100)

        // legacy test
        val legacyReportDefinitionInvalidIntervalResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionInvalidIntervalRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(
            legacyReportDefinitionInvalidIntervalResponse,
            RestStatus.BAD_REQUEST.status,
            "illegal_argument_exception"
        )
        Thread.sleep(100)
    }

    fun `test listing multiple report definitions`() {
        val reportDefinitionRequest = constructReportDefinitionRequest()
        val reportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = reportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)

        val secondReportDefinitionRequest = constructReportDefinitionRequest(name = "new report definition")
        val secondReportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            secondReportDefinitionRequest,
            RestStatus.OK.status
        )
        val newReportDefinitionId = secondReportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", newReportDefinitionId)
        Thread.sleep(1000)
        val listReportDefinitions = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_REPORTS_URI/definitions",
            "",
            RestStatus.OK.status
        )
        val totalHits = listReportDefinitions.get("totalHits").asInt
        Assert.assertEquals(2, totalHits)
        val reportDefinitionsList = listReportDefinitions.get("reportDefinitionDetailsList").asJsonArray
        Assert.assertEquals(
            reportDefinitionId,
            reportDefinitionsList[0].asJsonObject.get("id").asString
        )
        Assert.assertEquals(
            newReportDefinitionId,
            reportDefinitionsList[1].asJsonObject.get("id").asString
        )
    }

    fun `test legacy listing multiple report definitions`() {
        val reportDefinitionRequest = constructReportDefinitionRequest()
        val reportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = reportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)

        val secondReportDefinitionRequest = constructReportDefinitionRequest(name = "new report definition")
        val secondReportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            secondReportDefinitionRequest,
            RestStatus.OK.status
        )
        val newReportDefinitionId = secondReportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", newReportDefinitionId)
        Thread.sleep(1000)
        val listReportDefinitions = executeRequest(
            RestRequest.Method.GET.name,
            "$LEGACY_BASE_REPORTS_URI/definitions",
            "",
            RestStatus.OK.status
        )
        val totalHits = listReportDefinitions.get("totalHits").asInt
        Assert.assertEquals(totalHits, 2)
        val reportDefinitionsList = listReportDefinitions.get("reportDefinitionDetailsList").asJsonArray
        Assert.assertEquals(
            reportDefinitionId,
            reportDefinitionsList[0].asJsonObject.get("id").asString
        )
        Assert.assertEquals(
            newReportDefinitionId,
            reportDefinitionsList[1].asJsonObject.get("id").asString
        )
    }
}
