/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_ID_FIELD
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
 * Report Definition-delete response.
 * <pre> JSON format
 * {@code
 * {
 *   "reportDefinitionId":"reportDefinitionId"
 * }
 * }</pre>
 */
internal data class DeleteReportDefinitionResponse(
    val reportDefinitionId: String
) : BaseResponse() {

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(
        reportDefinitionId = input.readString()
    )

    companion object {
        private val log by logger(DeleteReportDefinitionResponse::class.java)

        /**
         * Parse the data from parser and create [DeleteReportDefinitionResponse] object
         * @param parser data referenced at parser
         * @return created [DeleteReportDefinitionResponse] object
         */
        fun parse(parser: XContentParser): DeleteReportDefinitionResponse {
            var reportDefinitionId: String? = null
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
                Metrics.REPORT_DEFINITION_DELETE_SYSTEM_ERROR.counter.increment()
                throw IllegalArgumentException("$REPORT_DEFINITION_ID_FIELD field absent")
            }
            return DeleteReportDefinitionResponse(reportDefinitionId)
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
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(REPORT_DEFINITION_ID_FIELD, reportDefinitionId)
            .endObject()
    }
}
