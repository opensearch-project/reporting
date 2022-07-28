/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.DeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParser.Token
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.common.xcontent.XContentType
import org.opensearch.reportsscheduler.model.ReportInstance
import org.opensearch.rest.RestRequest
import java.net.URI

internal fun StreamInput.createJsonParser(): XContentParser {
    return XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, this)
}

internal fun RestRequest.contentParserNextToken(): XContentParser {
    val parser = this.contentParser()
    parser.nextToken()
    return parser
}

internal fun XContentParser.stringList(): List<String> {
    val retList: MutableList<String> = mutableListOf()
    XContentParserUtils.ensureExpectedToken(Token.START_ARRAY, currentToken(), this)
    while (nextToken() != Token.END_ARRAY) {
        retList.add(text())
    }
    return retList
}

internal fun <T : Any> logger(forClass: Class<T>): Lazy<Logger> {
    return lazy { LogManager.getLogger(forClass) }
}

internal fun XContentBuilder.fieldIfNotNull(name: String, value: Any?): XContentBuilder {
    if (value != null) {
        this.field(name, value)
    }
    return this
}

internal fun XContentBuilder.objectIfNotNull(name: String, xContentObject: ToXContentObject?): XContentBuilder {
    if (xContentObject != null) {
        this.field(name)
        xContentObject.toXContent(this, ToXContent.EMPTY_PARAMS)
    }
    return this
}

/**
 * Helper function of notification feature, to build report link and send as message
 * @param origin [ReportInstance] url prefix
 * @param tenant [String]
 * @param reportInstanceId[String]
 * @return [String] the actual report link
 */
internal fun buildReportLink(origin: String, tenant: String, reportInstanceId: String): String {
    var tenantValueInUrl = tenant
    tenantValueInUrl = when (tenant) {
        "__user__" -> "private"
        "" -> "global"
        // Use this as equivalent to Javascript encodeURIComponents() https://stackoverflow.com/a/35294242
        else -> URI(null, null, tenantValueInUrl, null).rawPath
    }
    return "$origin/app/reports-dashboards?security_tenant=$tenantValueInUrl#/report_details/$reportInstanceId"
}
