/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler

import org.opensearch.common.xcontent.DeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentType
import org.opensearch.reportsscheduler.model.ReportDefinition
import org.opensearch.reportsscheduler.model.ReportDefinitionDetails
import org.opensearch.reportsscheduler.model.ReportInstance
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant

fun getJsonString(xContent: ToXContent): String {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        val builder = XContentFactory.jsonBuilder(byteArrayOutputStream)
        xContent.toXContent(builder, ToXContent.EMPTY_PARAMS)
        builder.close()
        return byteArrayOutputStream.toString("UTF8")
    }
}

inline fun <reified CreateType> createObjectFromJsonString(
    jsonString: String,
    block: (XContentParser) -> CreateType
): CreateType {
    val parser = XContentType.JSON.xContent()
        .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, jsonString)
    parser.nextToken()
    return block(parser)
}

internal fun createReportDefinitionObject(): ReportDefinition {
    val source = ReportDefinition.Source(
        description = "description",
        type = ReportDefinition.SourceType.Dashboard,
        origin = "http://localhost:5601",
        id = "id"
    )

    val format = ReportDefinition.Format(
        duration = Duration.parse("PT1H"),
        fileFormat = ReportDefinition.FileFormat.Pdf,
        limit = 1000,
        header = "optional header",
        footer = "optional footer"
    )

    val trigger = ReportDefinition.Trigger(
        triggerType = ReportDefinition.TriggerType.OnDemand,
        schedule = null
    )

    return ReportDefinition(
        name = "sample_report_definition",
        isEnabled = true,
        source = source,
        format = format,
        trigger = trigger,
        delivery = null
    )
}

internal fun createReportInstance(): ReportInstance {
    return ReportInstance(
        "id",
        Instant.ofEpochMilli(1603506908773),
        Instant.ofEpochMilli(1603506908773),
        Instant.ofEpochMilli(1603506908773),
        Instant.ofEpochMilli(1603506908773),
        tenant = "__user__",
        access = listOf(),
        null,
        ReportInstance.Status.Success,
        statusText = "200",
        inContextDownloadUrlPath = "/app/dashboard#view/dashboard-id"
    )
}

internal fun createReportDefinitionDetails(): ReportDefinitionDetails {
    return ReportDefinitionDetails(
        "id",
        Instant.ofEpochMilli(1603506908773),
        Instant.ofEpochMilli(1603506908773),
        "__user__",
        listOf(),
        reportDefinition = createReportDefinitionObject()
    )
}