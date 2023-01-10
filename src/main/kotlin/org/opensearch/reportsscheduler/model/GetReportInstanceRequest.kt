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
import org.opensearch.reportsscheduler.model.RestTag.REPORT_INSTANCE_ID_FIELD
import org.opensearch.reportsscheduler.util.logger
import java.io.IOException

/**
 * Get report instance info request
 * Data object created from GET request query params
 * <pre> JSON format
 * {@code
 * {
 *   "reportInstanceId":"reportInstanceId"
 * }
 * }</pre>
 */
internal class GetReportInstanceRequest(
    val reportInstanceId: String
) : ActionRequest(), ToXContentObject {

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(
        reportInstanceId = input.readString()
    )

    companion object {
        private val log by logger(GetReportInstanceRequest::class.java)

        /**
         * Parse the data from parser and create [GetReportInstanceRequest] object
         * @param parser data referenced at parser
         * @return created [GetReportInstanceRequest] object
         */
        fun parse(parser: XContentParser, useReportInstanceId: String? = null): GetReportInstanceRequest {
            var reportInstanceId: String? = useReportInstanceId
            XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
            while (Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    REPORT_INSTANCE_ID_FIELD -> reportInstanceId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                    }
                }
            }
            reportInstanceId ?: run {
                Metrics.REPORT_INSTANCE_INFO_USER_ERROR_INVALID_REPORT_ID.counter.increment()
                throw IllegalArgumentException("$REPORT_INSTANCE_ID_FIELD field absent")
            }

            return GetReportInstanceRequest(reportInstanceId)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(reportInstanceId)
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
            .field(REPORT_INSTANCE_ID_FIELD, reportInstanceId)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
