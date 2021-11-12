/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_FIELD
import org.opensearch.reportsscheduler.util.createJsonParser
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParser.Token
import org.opensearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Report Definition-create request.
 * <pre> JSON format
 * {@code
 * {
 *   "reportDefinition":{
 *      // refer [org.opensearch.reportsscheduler.model.ReportDefinition]
 *   }
 * }
 * }</pre>
 */
internal class CreateReportDefinitionRequest : ActionRequest, ToXContentObject {
    val reportDefinition: ReportDefinition

    companion object {
        private val log by logger(CreateReportDefinitionRequest::class.java)
    }

    constructor(reportDefinition: ReportDefinition) : super() {
        this.reportDefinition = reportDefinition
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [CreateReportDefinitionRequest] object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser) : super() {
        var reportDefinition: ReportDefinition? = null
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
        while (Token.END_OBJECT != parser.nextToken()) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                REPORT_DEFINITION_FIELD -> reportDefinition = ReportDefinition.parse(parser)
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                }
            }
        }
        reportDefinition ?: run {
            Metrics.REPORT_DEFINITION_CREATE_USER_ERROR.counter.increment()
            throw IllegalArgumentException("$REPORT_DEFINITION_FIELD field absent")
        }
        this.reportDefinition = reportDefinition
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        toXContent(XContentFactory.jsonBuilder(output), ToXContent.EMPTY_PARAMS)
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @param params XContent parameters
     * @return created XContentBuilder object
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(REPORT_DEFINITION_FIELD, reportDefinition)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
