/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import com.fasterxml.jackson.core.JsonParseException
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.reportsscheduler.createObjectFromJsonString
import org.opensearch.reportsscheduler.getJsonString
import org.opensearch.reportsscheduler.settings.PluginSettings

internal class GetAllReportDefinitionsRequestTests {
    private val sampleFromIndex = 2
    private val sampleMaxItems = 10

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val request = GetAllReportDefinitionsRequest(sampleFromIndex, sampleMaxItems)
        val jsonString = getJsonString(request)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportDefinitionsRequest.parse(it) }
        assertEquals(request.fromIndex, recreatedObject.fromIndex)
        assertEquals(request.maxItems, recreatedObject.maxItems)
    }

    @Test
    fun `Get request should deserialize json object using parser`() {
        val jsonString = """
        {
            "fromIndex": $sampleFromIndex,
            "maxItems": $sampleMaxItems
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportDefinitionsRequest.parse(it) }
        assertEquals(sampleFromIndex, recreatedObject.fromIndex)
        assertEquals(sampleMaxItems, recreatedObject.maxItems)
    }

    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        org.junit.jupiter.api.assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GetAllReportDefinitionsRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val jsonString = """
        {
            "fromIndex": $sampleFromIndex,
            "maxItems": $sampleMaxItems,
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetAllReportDefinitionsRequest.parse(it) }
        assertEquals(sampleFromIndex, recreatedObject.fromIndex)
        assertEquals(sampleMaxItems, recreatedObject.maxItems)
    }

    @Test
    fun `Get all requests should have default value according to plugin settings if not provided in request`() {
        val defaultMaxItems = PluginSettings.defaultItemsQueryCount
        val recreatedObject = createObjectFromJsonString("{}") { GetAllReportDefinitionsRequest.parse(it) }
        assertEquals(0, recreatedObject.fromIndex)
        assertEquals(defaultMaxItems, recreatedObject.maxItems)
    }
}
