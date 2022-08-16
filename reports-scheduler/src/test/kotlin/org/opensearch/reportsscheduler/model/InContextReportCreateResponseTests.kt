/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.createReportInstance
import org.opensearch.reportsscheduler.getJsonString

internal class InContextReportCreateResponseTests {

    @Test
    fun `Create response serialize and deserialize using json object should be equal`() {
        val reportInstance = createReportInstance()
        val response = InContextReportCreateResponse(reportInstance, false)
        val jsonString = getJsonString(response)
        val recreatedObject = createObjectFromJsonString(jsonString) { InContextReportCreateResponse(it) }
        Assertions.assertEquals(
            response.reportInstance,
            recreatedObject.reportInstance
        )
    }

    @Test
    fun `Create response should deserialize json object using parser`() {
        val reportInstance = createReportInstance()
        val jsonString = """
            {
                "reportInstance": 
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
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { InContextReportCreateResponse(it) }
        Assertions.assertEquals(reportInstance, recreatedObject.reportInstance)
    }

    @Test
    fun `Create response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { InContextReportCreateResponse(it) }
        }
    }

    @Test
    fun `Create response should safely ignore extra field in json object`() {
        val reportInstance = createReportInstance()
        val jsonString = """
            {
                "reportInstance": 
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
               },
               "extra_field_1":["extra", "value"],
               "extra_field_2":{"extra":"value"},
               "extra_field_3":"extra value 3"
            }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { InContextReportCreateResponse(it) }
        Assertions.assertEquals(reportInstance, recreatedObject.reportInstance)
    }
}
