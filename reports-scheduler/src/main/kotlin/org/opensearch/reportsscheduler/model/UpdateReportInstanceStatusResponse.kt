/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.model.RestTag.REPORT_INSTANCE_ID_FIELD
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParser.Token
import org.opensearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Update report instance status response.
 * <pre> JSON format
 * {@code
 * {
 *   "reportInstanceId":"reportInstanceId"
 * }
 * }</pre>
 */
internal data class UpdateReportInstanceStatusResponse(
    val reportInstanceId: String
) : BaseResponse() {

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(
        reportInstanceId = input.readString()
    )

    companion object {
        private val log by logger(UpdateReportInstanceStatusResponse::class.java)

        /**
         * Parse the data from parser and create [UpdateReportInstanceStatusResponse] object
         * @param parser data referenced at parser
         * @return created [UpdateReportInstanceStatusResponse] object
         */
        fun parse(parser: XContentParser): UpdateReportInstanceStatusResponse {
            var reportInstanceId: String? = null
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
            reportInstanceId ?: throw IllegalArgumentException("$REPORT_INSTANCE_ID_FIELD field absent")
            return UpdateReportInstanceStatusResponse(reportInstanceId)
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
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(REPORT_INSTANCE_ID_FIELD, reportInstanceId)
            .endObject()
    }
}
