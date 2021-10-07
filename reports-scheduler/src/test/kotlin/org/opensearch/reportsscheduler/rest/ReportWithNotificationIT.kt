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

package org.opensearch.reportsscheduler.rest

import org.junit.Assert
import org.opensearch.reportsscheduler.PluginRestTestCase
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.constructReportDefinitionRequest
import org.opensearch.reportsscheduler.jsonify
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

class ReportWithNotificationIT : PluginRestTestCase() {
    fun `test create on-demand report with notifications configured`() {
        val notificationsConfigRequest = """
            {
              "config": {
                "name": "Test channel",
                "description": "Dummy channel for reports and notifications integration testing.",
                "config_type": "slack",
                "feature_list": ["reports"],
                "is_enabled": true,
                "slack": {
                  "url": "https://test-webhook"
                }
              }
            }
        """.trimIndent()
        val notificationsConfigResponse = executeRequest(
            RestRequest.Method.POST.name,
            "_plugins/_notifications/configs",
            notificationsConfigRequest,
            RestStatus.OK.status
        )
        val notificationsConfigId = notificationsConfigResponse.get("config_id").asString
        Assert.assertNotNull("notificationsConfigId should be generated", notificationsConfigId)
        val trigger = """
                "trigger":{
                    "triggerType":"OnDemand"
                },
            """.trimIndent()
        val delivery = """
                "delivery":{
                    "title":"title",
                    "textDescription":"textDescription",
                    "htmlDescription":"optional htmlDescription",
                    "configIds":["optional_configIds"]
                },
            """.trimIndent()
        val reportDefinitionRequest = constructReportDefinitionRequest(trigger, "report_definition", delivery)
        val reportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = reportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)
        val onDemandResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_REPORTS_URI/on_demand/$reportDefinitionId",
            "{}",
            RestStatus.OK.status
        )
        Assert.assertNotNull("reportInstance should be generated", onDemandResponse)
        val reportInstance = onDemandResponse.get("reportInstance").asJsonObject
        val reportDefinitionDetails = reportInstance.get("reportDefinitionDetails").asJsonObject
        val reportDefinition = reportDefinitionDetails.get("reportDefinition").asJsonObject
        Assert.assertEquals(reportDefinitionId, reportDefinitionDetails.get("id").asString)
        Assert.assertEquals(jsonify(reportDefinitionRequest).get("reportDefinition").asJsonObject, reportDefinition)
        Thread.sleep(2000)
        val notificationEventResponse = executeRequest(
            RestRequest.Method.GET.name,
            "_plugins/_notifications/events",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(1, notificationEventResponse.get("total_hits").asInt)
        val notificationsStatusList = notificationEventResponse.getAsJsonArray("event_list").get(0).asJsonObject
            .getAsJsonObject("event").getAsJsonArray("status_list").get(0).asJsonObject
        Assert.assertEquals(notificationsConfigId, notificationsStatusList.get("config_id").asString)
        Assert.assertEquals(
            "500",
            notificationsStatusList.get("delivery_status").asJsonObject.get("status_code").asString
        )
    }
}
