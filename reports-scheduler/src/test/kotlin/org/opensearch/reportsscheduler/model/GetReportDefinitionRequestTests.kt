/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.getJsonString

internal class GetReportDefinitionRequestTests {

    @Test
    fun `Get request serialize and deserialize transport object should be equal`() {
        val request = GetReportDefinitionRequest("sample_report_definition_id")
        val recreatedObject = recreateObject(request) { GetReportDefinitionRequest(it) }
        assertEquals(request.reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val reportDefinitionResponse = GetReportDefinitionRequest("sample_report_definition_id")
        val jsonString = getJsonString(reportDefinitionResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetReportDefinitionRequest.parse(it) }
        assertEquals(reportDefinitionResponse.reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Get request should deserialize json object using parser`() {
        val reportDefinitionId = "sample_report_definition_id"
        val jsonString = "{\"reportDefinitionId\":\"$reportDefinitionId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { GetReportDefinitionRequest.parse(it) }
        assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetReportDefinitionRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should throw exception when reportDefinitionId is replace with reportDefinitionId2 in json object`() {
        val jsonString = "{\"reportDefinitionId2\":\"sample_report_definition_id\"}"
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { GetReportDefinitionRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val reportDefinitionId = "report_definition_id"
        val jsonString = """
        {
            "reportDefinitionId":"$reportDefinitionId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetReportDefinitionRequest.parse(it) }
        assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }
}