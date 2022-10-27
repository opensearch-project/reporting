/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParser.Token
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.model.RestTag.REPORT_INSTANCE_FIELD
import org.opensearch.reportsscheduler.util.createJsonParser
import org.opensearch.reportsscheduler.util.logger
import java.io.IOException

/**
 * Report-create response for in-context menu.
 * <pre> JSON format
 * {@code
 * {
 *   "reportInstance":{
 *      // refer [org.opensearch.reportsscheduler.model.ReportInstance]
 *   }
 * }
 * }</pre>
 */
internal class InContextReportCreateResponse : BaseResponse {
    val reportInstance: ReportInstance
    private val filterSensitiveInfo: Boolean

    companion object {
        private val log by logger(InContextReportCreateResponse::class.java)
    }

    constructor(reportInstance: ReportInstance, filterSensitiveInfo: Boolean) : super() {
        this.reportInstance = reportInstance
        this.filterSensitiveInfo = filterSensitiveInfo
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [InContextReportCreateResponse] object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser) : super() {
        var reportInstance: ReportInstance? = null
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
        while (Token.END_OBJECT != parser.nextToken()) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                REPORT_INSTANCE_FIELD -> reportInstance = ReportInstance.parse(parser)
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                }
            }
        }
        reportInstance ?: throw IllegalArgumentException("$REPORT_INSTANCE_FIELD field absent")
        this.reportInstance = reportInstance
        this.filterSensitiveInfo = false // Sensitive info Must have filtered when created json object
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        toXContent(XContentFactory.jsonBuilder(output), ToXContent.EMPTY_PARAMS)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        val xContentParams = if (filterSensitiveInfo) {
            RestTag.FILTERED_REST_OUTPUT_PARAMS
        } else {
            RestTag.REST_OUTPUT_PARAMS
        }
        builder!!.startObject()
            .field(REPORT_INSTANCE_FIELD)
        reportInstance.toXContent(builder, xContentParams)
        return builder.endObject()
    }
}
