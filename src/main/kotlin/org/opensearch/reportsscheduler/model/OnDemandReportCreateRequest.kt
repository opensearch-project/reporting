/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

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
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_ID_FIELD
import org.opensearch.reportsscheduler.util.logger
import java.io.IOException

/**
 * Report-create request from on-demand report definition.
 * reportDefinitionId field is from request query params
 * <pre> JSON format
 * {@code
 * {
 *   "reportDefinitionId":"reportDefinitionId"
 * }
 * }</pre>
 */
internal class OnDemandReportCreateRequest(
    val reportDefinitionId: String
) : ActionRequest(), ToXContentObject {

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(
        reportDefinitionId = input.readString()
    )

    companion object {
        private val log by logger(OnDemandReportCreateRequest::class.java)

        /**
         * Parse the data from parser and create [OnDemandReportCreateRequest] object
         * @param parser data referenced at parser
         * @param useReportDefinitionId use this id if not available in the json
         * @return created [OnDemandReportCreateRequest] object
         */
        fun parse(parser: XContentParser, useReportDefinitionId: String? = null): OnDemandReportCreateRequest {
            var reportDefinitionId: String? = useReportDefinitionId
            XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
            while (Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    REPORT_DEFINITION_ID_FIELD -> reportDefinitionId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                    }
                }
            }
            reportDefinitionId ?: run {
                Metrics.REPORT_FROM_DEFINITION_ID_USER_ERROR_INVALID_REPORT_DEF_ID.counter.increment()
                throw IllegalArgumentException("$REPORT_DEFINITION_ID_FIELD field absent")
            }

            return OnDemandReportCreateRequest(reportDefinitionId)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(reportDefinitionId)
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
            .field(REPORT_DEFINITION_ID_FIELD, reportDefinitionId)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
