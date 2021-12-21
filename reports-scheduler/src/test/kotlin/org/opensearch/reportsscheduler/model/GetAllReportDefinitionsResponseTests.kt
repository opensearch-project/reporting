/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.createReportDefinitionDetails
import org.opensearch.reportsscheduler.getJsonString

internal class GetAllReportDefinitionsResponseTests {

    private fun createReportDefinitionDetailsSearchResults(): ReportDefinitionDetailsSearchResults {
        return ReportDefinitionDetailsSearchResults(
            startIndex = 0,
            totalHits = 100,
            totalHitRelation = TotalHits.Relation.EQUAL_TO,
            objectList = listOf(createReportDefinitionDetails())
        )
    }

    @Test
    fun `Get response serialize and deserialize using json object should be equal`() {
        val reportDefinitionDetailsSearchResults = createReportDefinitionDetailsSearchResults()
        val response = GetAllReportDefinitionsResponse(reportDefinitionDetailsSearchResults, false)
        val jsonString = getJsonString(response)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportDefinitionsResponse(it) }
        Assertions.assertEquals(
            response.reportDefinitionList.objectList,
            recreatedObject.reportDefinitionList.objectList
        )
    }

    @Test
    fun `Get response should deserialize json object using parser`() {
        val reportDefinitionDetailsSearchResults = createReportDefinitionDetailsSearchResults()
        val jsonString = """
            {
                "startIndex":"0",
                "totalHits":"100",
                "totalHitRelation":"eq",
                "reportDefinitionDetailsList": [
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
               ]
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportDefinitionsResponse(it) }
        Assertions.assertEquals(reportDefinitionDetailsSearchResults.objectList, recreatedObject.reportDefinitionList.objectList)
    }

    @Test
    fun `Get response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetAllReportDefinitionsResponse(it) }
        }
    }

    @Test
    fun `Get response should safely ignore extra field in json object`() {
        val reportDefinitionDetailsSearchResults = createReportDefinitionDetailsSearchResults()
        val jsonString = """
            {
                "startIndex":"0",
                "totalHits":"100",
                "totalHitRelation":"eq",
                "reportDefinitionDetailsList": [
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
               ],
              "extra_field_1":["extra", "value"],
              "extra_field_2":{"extra":"value"},
              "extra_field_3":"extra value 3"
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportDefinitionsResponse(it) }
        Assertions.assertEquals(reportDefinitionDetailsSearchResults.objectList, recreatedObject.reportDefinitionList.objectList)
    }
}