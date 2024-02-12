/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HelpersTests {
    private val sampleOrigin = "https://localhost:5601/base/path"
    private val sampleId = "123456qwert"
    private var sampleTenant = ""

    @Test
    fun `test build report link works with global tenant`() {
        assertEquals(
            "$sampleOrigin/app/reports-dashboards?security_tenant=global#/report_details/$sampleId",
            buildReportLink(sampleOrigin, sampleTenant, sampleId)
        )
    }

    @Test
    fun `test build report link works with private tenant`() {
        sampleTenant = "__user__"
        assertEquals(
            "$sampleOrigin/app/reports-dashboards?security_tenant=private#/report_details/$sampleId",
            buildReportLink(sampleOrigin, sampleTenant, sampleId)
        )
    }

    @Test
    fun `test build report link works with custom tenant and encodes it`() {
        sampleTenant = "custom name"
        val reportLink = buildReportLink(sampleOrigin, sampleTenant, sampleId)
        assertEquals(
            "$sampleOrigin/app/reports-dashboards?security_tenant=custom%20name#/report_details/$sampleId",
            reportLink
        )
    }
}
