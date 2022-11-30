/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.createReportDefinitionDetails
import org.opensearch.reportsscheduler.getJsonString

internal class GetReportDefinitionResponseTests {

    @Test
    fun `Get response serialize and deserialize using json object should be equal`() {
        val reportDefinitionDetails = createReportDefinitionDetails()
        val reportDefinitionResponse = GetReportDefinitionResponse(reportDefinitionDetails, false)
        val jsonString = getJsonString(reportDefinitionResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetReportDefinitionResponse(it) }
        assertEquals(reportDefinitionResponse.reportDefinitionDetails, recreatedObject.reportDefinitionDetails)
    }

    @Test
    fun `Get response should deserialize json object using parser`() {
        val reportDefinitionDetails = createReportDefinitionDetails()
        val jsonString = """
            {
                "reportDefinitionDetails":
                {
                   "id":"id",
                   "lastUpdatedTimeMs":1603506908773,
                   "createdTimeMs":1603506908773,
                   "tenant":"__user__",
                   "access":[],    
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
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetReportDefinitionResponse(it) }
        assertEquals(reportDefinitionDetails, recreatedObject.reportDefinitionDetails)
    }

    @Test
    fun `Get response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetReportDefinitionResponse(it) }
        }
    }

    @Test
    fun `Get response should safely ignore extra field in json object`() {
        val reportDefinitionDetails = createReportDefinitionDetails()
        val jsonString = """
            {
                "reportDefinitionDetails":
                {
                   "id":"id",
                   "lastUpdatedTimeMs":1603506908773,
                   "createdTimeMs":1603506908773,
                   "tenant":"__user__",
                   "access":[],    
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
                },
                "extra_field_1":["extra", "value"],
                "extra_field_2":{"extra":"value"},
                "extra_field_3":"extra value 3"
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetReportDefinitionResponse(it) }
        assertEquals(reportDefinitionDetails, recreatedObject.reportDefinitionDetails)
    }
}