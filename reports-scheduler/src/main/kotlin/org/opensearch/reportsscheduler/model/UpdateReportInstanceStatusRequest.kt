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
import org.opensearch.reportsscheduler.model.ReportInstance.Status
import org.opensearch.reportsscheduler.model.RestTag.REPORT_INSTANCE_ID_FIELD
import org.opensearch.reportsscheduler.model.RestTag.STATUS_FIELD
import org.opensearch.reportsscheduler.model.RestTag.STATUS_TEXT_FIELD
import org.opensearch.reportsscheduler.util.fieldIfNotNull
import org.opensearch.reportsscheduler.util.logger
import java.io.IOException

/**
 * Update report instance status request
 * <pre> JSON format
 * {@code
 * {
 *   "reportInstanceId":"reportInstanceId"
 *   "status":"Success", // refer [Status]
 *   "statusText":"Operation completed",
 * }
 * }</pre>
 */
internal class UpdateReportInstanceStatusRequest(
    val reportInstanceId: String,
    var status: Status,
    var statusText: String? = null
) : ActionRequest(), ToXContentObject {

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(
        reportInstanceId = input.readString(),
        status = input.readEnum(Status::class.java),
        statusText = input.readOptionalString()
    )

    companion object {
        private val log by logger(UpdateReportInstanceStatusRequest::class.java)

        /**
         * Parse the data from parser and create [UpdateReportInstanceStatusRequest] object
         * @param parser data referenced at parser
         * @return created [UpdateReportInstanceStatusRequest] object
         */
        fun parse(parser: XContentParser, useReportInstanceId: String? = null): UpdateReportInstanceStatusRequest {
            var reportInstanceId: String? = useReportInstanceId
            var status: Status? = null
            var statusText: String? = null
            XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
            while (Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    REPORT_INSTANCE_ID_FIELD -> reportInstanceId = parser.text()
                    STATUS_FIELD -> status = Status.valueOf(parser.text())
                    STATUS_TEXT_FIELD -> statusText = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                    }
                }
            }
            reportInstanceId ?: run {
                Metrics.REPORT_INSTANCE_UPDATE_USER_ERROR_INVALID_REPORT_ID.counter.increment()
                throw IllegalArgumentException("$REPORT_INSTANCE_ID_FIELD field absent")
            }
            status ?: run {
                Metrics.REPORT_INSTANCE_UPDATE_USER_ERROR_INVALID_STATUS.counter.increment()
                throw IllegalArgumentException("$STATUS_FIELD field absent")
            }
            return UpdateReportInstanceStatusRequest(reportInstanceId, status, statusText)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(reportInstanceId)
        output.writeEnum(status)
        output.writeOptionalString(statusText)
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
            .field(STATUS_FIELD, status)
            .fieldIfNotNull(STATUS_TEXT_FIELD, statusText)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
