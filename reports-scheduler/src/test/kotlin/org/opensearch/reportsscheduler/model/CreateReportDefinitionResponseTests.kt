/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.recreateObject
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.getJsonString

internal class CreateReportDefinitionResponseTests {

    @Test
    fun `Create response serialize and deserialize transport object should be equal`() {
        val reportDefinitionResponse = CreateReportDefinitionResponse("sample_report_definition_id")
        val recreatedObject = recreateObject(reportDefinitionResponse) { CreateReportDefinitionResponse(it) }
        Assertions.assertEquals(reportDefinitionResponse.reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Create response serialize and deserialize using json object should be equal`() {
        val reportDefinitionResponse = CreateReportDefinitionResponse("sample_report_definition_id")
        val jsonString = getJsonString(reportDefinitionResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateReportDefinitionResponse.parse(it) }
        Assertions.assertEquals(reportDefinitionResponse.reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Create response should deserialize json object using parser`() {
        val reportDefinitionId = "sample_report_definition_id"
        val jsonString = "{\"reportDefinitionId\":\"$reportDefinitionId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateReportDefinitionResponse.parse(it) }
        Assertions.assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Create response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { CreateReportDefinitionResponse.parse(it) }
        }
    }

    @Test
    fun `Create response should throw exception when reportDefinitionId is replace with reportDefinitionId2 in json object`() {
        val jsonString = "{\"reportDefinitionId2\":\"sample_report_definition_id\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { CreateReportDefinitionResponse.parse(it) }
        }
    }

    @Test
    fun `Create response should safely ignore extra field in json object`() {
        val reportDefinitionId = "report_definition_id"
        val jsonString = """
        {
            "reportDefinitionId":"$reportDefinitionId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateReportDefinitionResponse.parse(it) }
        Assertions.assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }
}
