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

internal class UpdateReportDefinitionResponseTests {
    @Test
    fun `Update response serialize and deserialize transport object should be equal`() {
        val reportDefinitionResponse = UpdateReportDefinitionResponse("sample_report_definition_id")
        val recreatedObject = recreateObject(reportDefinitionResponse) { UpdateReportDefinitionResponse(it) }
        Assertions.assertEquals(reportDefinitionResponse.reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Update response serialize and deserialize using json object should be equal`() {
        val reportDefinitionResponse = UpdateReportDefinitionResponse("sample_report_definition_id")
        val jsonString = getJsonString(reportDefinitionResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportDefinitionResponse.parse(it) }
        Assertions.assertEquals(reportDefinitionResponse.reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Update response should deserialize json object using parser`() {
        val reportDefinitionId = "sample_report_definition_id"
        val jsonString = "{\"reportDefinitionId\":\"$reportDefinitionId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportDefinitionResponse.parse(it) }
        Assertions.assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Update response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateReportDefinitionResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should throw exception when reportDefinitionId is replace with reportDefinitionId2 in json object`() {
        val jsonString = "{\"reportDefinitionId2\":\"sample_report_definition_id\"}"
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { UpdateReportDefinitionResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should safely ignore extra field in json object`() {
        val reportDefinitionId = "report_definition_id"
        val jsonString = """
        {
            "reportDefinitionId":"$reportDefinitionId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportDefinitionResponse.parse(it) }
        Assertions.assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }
}
