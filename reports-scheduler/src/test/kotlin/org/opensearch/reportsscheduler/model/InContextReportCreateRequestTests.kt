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
import org.opensearch.reportsscheduler.getJsonString
import java.time.Instant

internal class InContextReportCreateRequestTests {
    private val request = InContextReportCreateRequest(
        beginTime = Instant.ofEpochMilli(1603506908773),
        endTime = Instant.ofEpochMilli(1603506908773),
        reportDefinitionDetails = null,
        status = ReportInstance.Status.Success,
        statusText = "200",
        inContextDownloadUrlPath = "/app/dashboard#view/dashboard-id"
    )

    private fun verify(request: InContextReportCreateRequest, recreatedObject: InContextReportCreateRequest) {
        Assertions.assertEquals(request.beginTime, recreatedObject.beginTime)
        Assertions.assertEquals(request.endTime, recreatedObject.endTime)
        Assertions.assertEquals(request.reportDefinitionDetails, recreatedObject.reportDefinitionDetails)
        Assertions.assertEquals(request.status, recreatedObject.status)
        Assertions.assertEquals(request.statusText, recreatedObject.statusText)
        Assertions.assertEquals(request.inContextDownloadUrlPath, recreatedObject.inContextDownloadUrlPath)
    }

    @Test
    fun `Create request serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(request)
        val recreatedObject = createObjectFromJsonString(jsonString) { InContextReportCreateRequest(it) }
        verify(request, recreatedObject)
    }

    @Test
    fun `Create request should deserialize json object using parser`() {
        val jsonString = """
         {
               "beginTimeMs":1603506908773,
               "endTimeMs":1603506908773,
               "status": "Success",
               "statusText":"200",
               "inContextDownloadUrlPath":"/app/dashboard#view/dashboard-id"
         }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { InContextReportCreateRequest(it) }
        verify(request, recreatedObject)
    }

    @Test
    fun `Create request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { InContextReportCreateRequest(it) }
        }
    }

    @Test
    fun `Create request should safely ignore extra field in json object`() {
        val jsonString = """
        {
            "beginTimeMs":1603506908773,
            "endTimeMs":1603506908773,
            "status": "Success",
            "statusText":"200",
            "inContextDownloadUrlPath":"/app/dashboard#view/dashboard-id",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()

        val recreatedObject = createObjectFromJsonString(jsonString) { InContextReportCreateRequest(it) }
        verify(request, recreatedObject)
    }
}