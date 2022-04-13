/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.getJsonString

internal class UpdateReportInstanceStatusRequestTests {
    private val request = UpdateReportInstanceStatusRequest(
        reportInstanceId = "sample_report_instance_id",
        status = ReportInstance.Status.Success,
        statusText = "200"
    )

    private fun verify(request: UpdateReportInstanceStatusRequest, recreatedObject: UpdateReportInstanceStatusRequest) {
        Assertions.assertEquals(request.reportInstanceId, recreatedObject.reportInstanceId)
        Assertions.assertEquals(request.status, recreatedObject.status)
        Assertions.assertEquals(request.statusText, recreatedObject.statusText)
    }

    @Test
    fun `Create request serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(request)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusRequest.parse(it) }
        verify(request, recreatedObject)
    }

    @Test
    fun `Create request should deserialize json object using parser`() {
        val jsonString = """
         {
               "reportInstanceId":"sample_report_instance_id",
               "status": "Success",
               "statusText":"200"
         }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusRequest.parse(it) }
        verify(request, recreatedObject)
    }

    @Test
    fun `Create request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusRequest.parse(it) }
        }
    }

    @Test
    fun `Create request should safely ignore extra field in json object`() {
        val jsonString = """
        {
            "reportInstanceId":"sample_report_instance_id",
            "status": "Success",
            "statusText":"200",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()

        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateReportInstanceStatusRequest.parse(it) }
        verify(request, recreatedObject)
    }
}
