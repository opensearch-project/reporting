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
import org.opensearch.reportsscheduler.model.RestTag.BEGIN_TIME_FIELD
import org.opensearch.reportsscheduler.model.RestTag.END_TIME_FIELD
import org.opensearch.reportsscheduler.model.RestTag.IN_CONTEXT_DOWNLOAD_URL_FIELD
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_DETAILS_FIELD
import org.opensearch.reportsscheduler.model.RestTag.REST_OUTPUT_PARAMS
import org.opensearch.reportsscheduler.model.RestTag.STATUS_FIELD
import org.opensearch.reportsscheduler.model.RestTag.STATUS_TEXT_FIELD
import org.opensearch.reportsscheduler.util.createJsonParser
import org.opensearch.reportsscheduler.util.fieldIfNotNull
import org.opensearch.reportsscheduler.util.logger
import java.io.IOException
import java.time.Instant

/**
 * Report-create request from in-context menu.
 * <pre> JSON format
 * {@code
 * {
 *   "beginTimeMs":1603506908773,
 *   "endTimeMs":1603506908773,
 *   "reportDefinitionDetails":{
 *      // refer [org.opensearch.reportsscheduler.model.ReportDefinitionDetails]
 *   },
 *   "status":"Success", // Scheduled, Executing, Success, Failed
 *   "statusText":"Success",
 *   "inContextDownloadUrlPath":"/app/dashboard#view/dashboard-id"
 * }
 * }</pre>
 */
internal class InContextReportCreateRequest : ActionRequest, ToXContentObject {
    val beginTime: Instant
    val endTime: Instant
    val reportDefinitionDetails: ReportDefinitionDetails?
    val status: Status
    val statusText: String?
    val inContextDownloadUrlPath: String?

    companion object {
        private val log by logger(InContextReportCreateRequest::class.java)
    }

    constructor(
        beginTime: Instant,
        endTime: Instant,
        reportDefinitionDetails: ReportDefinitionDetails?,
        status: Status,
        statusText: String? = null,
        inContextDownloadUrlPath: String? = null
    ) : super() {
        this.beginTime = beginTime
        this.endTime = endTime
        this.reportDefinitionDetails = reportDefinitionDetails
        this.status = status
        this.statusText = statusText
        this.inContextDownloadUrlPath = inContextDownloadUrlPath
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [InContextReportCreateRequest] object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser) : super() {
        var beginTime: Instant? = null
        var endTime: Instant? = null
        var reportDefinitionDetails: ReportDefinitionDetails? = null
        var status: Status? = null
        var statusText: String? = null
        var inContextDownloadUrlPath: String? = null
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
        while (Token.END_OBJECT != parser.nextToken()) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                BEGIN_TIME_FIELD -> beginTime = Instant.ofEpochMilli(parser.longValue())
                END_TIME_FIELD -> endTime = Instant.ofEpochMilli(parser.longValue())
                REPORT_DEFINITION_DETAILS_FIELD -> reportDefinitionDetails = ReportDefinitionDetails.parse(parser)
                STATUS_FIELD -> status = Status.valueOf(parser.text())
                STATUS_TEXT_FIELD -> statusText = parser.text()
                IN_CONTEXT_DOWNLOAD_URL_FIELD -> inContextDownloadUrlPath = parser.text()
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:InContextReportCreateRequest Skipping Unknown field $fieldName")
                }
            }
        }
        beginTime ?: run {
            Metrics.REPORT_FROM_DEFINITION_USER_ERROR_INVALID_BEGIN_TIME.counter.increment()
            throw IllegalArgumentException("$BEGIN_TIME_FIELD field absent")
        }
        endTime ?: run {
            Metrics.REPORT_FROM_DEFINITION_USER_ERROR_INVALID_END_TIME.counter.increment()
            throw IllegalArgumentException("$END_TIME_FIELD field absent")
        }
        status ?: run {
            Metrics.REPORT_FROM_DEFINITION_USER_ERROR_INVALID_STATUS.counter.increment()
            throw IllegalArgumentException("$STATUS_FIELD field absent")
        }
        this.beginTime = beginTime
        this.endTime = endTime
        this.reportDefinitionDetails = reportDefinitionDetails
        this.status = status
        this.statusText = statusText
        this.inContextDownloadUrlPath = inContextDownloadUrlPath
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
        builder!!.startObject()
            .field(BEGIN_TIME_FIELD, beginTime.toEpochMilli())
            .field(END_TIME_FIELD, endTime.toEpochMilli())
        if (reportDefinitionDetails != null) {
            builder.field(REPORT_DEFINITION_DETAILS_FIELD)
            reportDefinitionDetails.toXContent(builder, REST_OUTPUT_PARAMS)
        }
        return builder.field(STATUS_FIELD, status.name)
            .fieldIfNotNull(STATUS_TEXT_FIELD, statusText)
            .fieldIfNotNull(IN_CONTEXT_DOWNLOAD_URL_FIELD, inContextDownloadUrlPath)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
