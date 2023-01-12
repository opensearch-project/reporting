/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.getJsonString

internal class UpdateReportInstanceStatusResponseTests {

    @Test
    fun `Update response serialize and deserialize transport object should be equal`() {
        val response = UpdateReportInstanceStatusResponse("sample_report_instance_id")
        val recreatedObject = recreateObject(response) { UpdateReportInstanceStatusResponse(it) }
        Assertions.assertEquals(response.reportInstanceId, recreatedObject.reportInstanceId)
    }

    @Test
    fun `Update response serialize and deserialize using json object should be equal`() {
        val response = UpdateReportInstanceStatusResponse("sample_report_instance_id")
        val jsonString = getJsonString(response)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusResponse.parse(it) }
        Assertions.assertEquals(response.reportInstanceId, recreatedObject.reportInstanceId)
    }

    @Test
    fun `Update response should deserialize json object using parser`() {
        val reportInstanceId = "sample_report_instance_id"
        val jsonString = "{\"reportInstanceId\":\"$reportInstanceId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusResponse.parse(it) }
        Assertions.assertEquals(reportInstanceId, recreatedObject.reportInstanceId)
    }

    @Test
    fun `Update response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should throw exception when reportInstanceId is replace with reportInstanceId2 in json object`() {
        val jsonString = "{\"reportInstanceId2\":\"sample_report_instance_id\"}"
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should safely ignore extra field in json object`() {
        val reportInstanceId = "report_instance_id"
        val jsonString = """
        {
            "reportInstanceId":"$reportInstanceId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusResponse.parse(it) }
        Assertions.assertEquals(reportInstanceId, recreatedObject.reportInstanceId)
    }
}
