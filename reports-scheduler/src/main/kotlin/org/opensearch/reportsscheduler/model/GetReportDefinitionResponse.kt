/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.util.createJsonParser
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParser.Token
import org.opensearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Get report Definition info response.
 * <pre> JSON format
 * {@code
 * {
 *   "reportDefinitionDetails":{
 *      // refer [org.opensearch.reportsscheduler.model.ReportDefinitionDetails]
 *   }
 * }
 * }</pre>
 */
internal class GetReportDefinitionResponse : BaseResponse {
    val reportDefinitionDetails: ReportDefinitionDetails
    private val filterSensitiveInfo: Boolean

    companion object {
        private val log by logger(GetReportDefinitionResponse::class.java)
    }

    constructor(reportDefinition: ReportDefinitionDetails, filterSensitiveInfo: Boolean) : super() {
        this.reportDefinitionDetails = reportDefinition
        this.filterSensitiveInfo = filterSensitiveInfo
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [GetReportDefinitionResponse] object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser) : super() {
        var reportDefinition: ReportDefinitionDetails? = null
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
        while (Token.END_OBJECT != parser.nextToken()) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                RestTag.REPORT_DEFINITION_DETAILS_FIELD -> reportDefinition = ReportDefinitionDetails.parse(parser)
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                }
            }
        }
        reportDefinition ?: run {
            Metrics.REPORT_DEFINITION_INFO_SYSTEM_ERROR.counter.increment()
            throw IllegalArgumentException("${RestTag.REPORT_DEFINITION_FIELD} field absent")
        }
        this.reportDefinitionDetails = reportDefinition
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
            .field(RestTag.REPORT_DEFINITION_DETAILS_FIELD)
        reportDefinitionDetails.toXContent(builder, xContentParams)
        return builder.endObject()
    }
}
