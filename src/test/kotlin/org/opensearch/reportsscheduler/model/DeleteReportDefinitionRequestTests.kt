/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.getJsonString

internal class DeleteReportDefinitionRequestTests {

    @Test
    fun `Delete request serialize and deserialize using json object should be equal`() {
        val deleteRequest = DeleteReportDefinitionRequest("sample_report_definition_id")
        val jsonString = getJsonString(deleteRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteReportDefinitionRequest.parse(it) }
        assertEquals(deleteRequest.reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Delete request should deserialize json object using parser`() {
        val reportDefinitionId = "sample_report_definition_id"
        val jsonString = """
        {
            "reportDefinitionId":"$reportDefinitionId"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteReportDefinitionRequest.parse(it) }
        assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }

    @Test
    fun `Delete request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { DeleteReportDefinitionRequest.parse(it) }
        }
    }

    @Test
    fun `Delete request should safely ignore extra field in json object`() {
        val reportDefinitionId = "sample_report_definition_id"
        val jsonString = """
        {
            "reportDefinitionId":"$reportDefinitionId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteReportDefinitionRequest.parse(it) }
        assertEquals(reportDefinitionId, recreatedObject.reportDefinitionId)
    }
}
