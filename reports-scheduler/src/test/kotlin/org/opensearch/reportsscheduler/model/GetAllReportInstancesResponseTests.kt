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
import org.opensearch.reportsscheduler.createReportInstance
import org.opensearch.reportsscheduler.getJsonString

internal class GetAllReportInstancesResponseTests {

    private fun createReportInstanceSearchResults(): ReportInstanceSearchResults {

        return ReportInstanceSearchResults(
            startIndex = 0,
            totalHits = 100,
            totalHitRelation = TotalHits.Relation.EQUAL_TO,
            objectList = listOf(createReportInstance())
        )
    }

    @Test
    fun `Get response serialize and deserialize using json object should be equal`() {
        val reportInstanceSearchResults = createReportInstanceSearchResults()
        val response = GetAllReportInstancesResponse(reportInstanceSearchResults, false)
        val jsonString = getJsonString(response)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportInstancesResponse(it) }
        Assertions.assertEquals(
            response.reportInstanceList.objectList,
            recreatedObject.reportInstanceList.objectList
        )
    }

    @Test
    fun `Get response should deserialize json object using parser`() {
        val reportInstanceSearchResults = createReportInstanceSearchResults()
        val jsonString = """
            {
                "startIndex":"0",
                "totalHits":"100",
                "totalHitRelation":"eq",
                "reportInstanceList": [
                    {
                       "id":"id",
                       "lastUpdatedTimeMs":1603506908773,
                       "createdTimeMs":1603506908773,
                       "beginTimeMs":1603506908773,
                       "endTimeMs":1603506908773,
                       "tenant":"__user__",
                       "access":[],    
                       "status": "Success",
                       "statusText":"200",
                       "inContextDownloadUrlPath":"/app/dashboard#view/dashboard-id"
                   }
               ]
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportInstancesResponse(it) }
        Assertions.assertEquals(reportInstanceSearchResults.objectList, recreatedObject.reportInstanceList.objectList)
    }

    @Test
    fun `Get response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetAllReportInstancesResponse(it) }
        }
    }

    @Test
    fun `Get response should safely ignore extra field in json object`() {
        val reportInstanceSearchResults = createReportInstanceSearchResults()
        val jsonString = """
            {
                "startIndex":"0",
                "totalHits":"100",
                "totalHitRelation":"eq",
                "reportInstanceList": [
                    {
                       "id":"id",
                       "lastUpdatedTimeMs":1603506908773,
                       "createdTimeMs":1603506908773,
                       "beginTimeMs":1603506908773,
                       "endTimeMs":1603506908773,
                       "tenant":"__user__",
                       "access":[],    
                       "status": "Success",
                       "statusText":"200",
                       "inContextDownloadUrlPath":"/app/dashboard#view/dashboard-id"
                   }
               ],
              "extra_field_1":["extra", "value"],
              "extra_field_2":{"extra":"value"},
              "extra_field_3":"extra value 3"
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportInstancesResponse(it) }
        Assertions.assertEquals(reportInstanceSearchResults.objectList, recreatedObject.reportInstanceList.objectList)
    }
}