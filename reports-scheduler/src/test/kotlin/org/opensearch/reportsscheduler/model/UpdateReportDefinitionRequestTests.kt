/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.createReportDefinitionObject
import org.opensearch.reportsscheduler.getJsonString

internal class UpdateReportDefinitionRequestTests {

    private val reportDefinitionRequest = UpdateReportDefinitionRequest(
        "sample_report_definition_id",
        createReportDefinitionObject()
    )

    @Test
    fun `Update request serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(reportDefinitionRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportDefinitionRequest(it) }
        Assertions.assertEquals(reportDefinitionRequest.reportDefinition, recreatedObject.reportDefinition)
    }

    @Test
    fun `Update request should deserialize json object using parser`() {
        val jsonString = """
            {
                "reportDefinitionId": "sample_report_definition_id",
                "reportDefinition":{
                    "name":"sample_report_definition",
                    "isEnabled":true,
                    "source":{
                        "description":"description",
                        "type":"Dashboard",
                        "origin":"http://localhost:5601",
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
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportDefinitionRequest(it) }
        Assertions.assertEquals(reportDefinitionRequest.reportDefinition, recreatedObject.reportDefinition)
    }

    @Test
    fun `Update request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateReportDefinitionRequest(it) }
        }
    }

    @Test
    fun `Update request should safely ignore extra field in json object`() {
        val jsonString = """
            {
                "reportDefinitionId": "sample_report_definition_id",
                "reportDefinition":{
                    "name":"sample_report_definition",
                    "isEnabled":true,
                    "source":{
                        "description":"description",
                        "type":"Dashboard",
                        "origin":"http://localhost:5601",
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
                },
                "extra_field_1":["extra", "value"],
                "extra_field_2":{"extra":"value"},
                "extra_field_3":"extra value 3"
            }
        """.trimIndent()

        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportDefinitionRequest(it) }
        Assertions.assertEquals(reportDefinitionRequest.reportDefinition, recreatedObject.reportDefinition)
    }
}
