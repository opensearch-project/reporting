/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.RestTag.FROM_INDEX_FIELD
import org.opensearch.reportsscheduler.model.RestTag.MAX_ITEMS_FIELD
import org.opensearch.reportsscheduler.settings.PluginSettings
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
 * Get All report instances info request
 * Data object created from GET request query params
 * <pre> JSON format
 * {@code
 * {
 *   "fromIndex":100,
 *   "maxItems":100
 * }
 * }</pre>
 */
internal data class GetAllReportInstancesRequest(
    val fromIndex: Int,
    val maxItems: Int
) : ActionRequest(), ToXContentObject {

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(
        fromIndex = input.readInt(),
        maxItems = input.readInt()
    )

    companion object {
        private val log by logger(GetAllReportInstancesRequest::class.java)

        /**
         * Parse the data from parser and create [GetAllReportInstancesRequest] object
         * @param parser data referenced at parser
         * @return created [GetAllReportInstancesRequest] object
         */
        fun parse(parser: XContentParser): GetAllReportInstancesRequest {
            var fromIndex = 0
            var maxItems = PluginSettings.defaultItemsQueryCount
            XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
            while (Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    FROM_INDEX_FIELD -> fromIndex = parser.intValue()
                    MAX_ITEMS_FIELD -> maxItems = parser.intValue()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                    }
                }
            }
            return GetAllReportInstancesRequest(fromIndex, maxItems)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeInt(fromIndex)
        output.writeInt(maxItems)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return if (fromIndex < 0) {
            val exception = ActionRequestValidationException()
            exception.addValidationError("fromIndex should be greater than 0")
            Metrics.REPORT_INSTANCE_LIST_USER_ERROR_INVALID_FROM_INDEX.counter.increment()
            exception
        } else {
            null
        }
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
            .field(FROM_INDEX_FIELD, fromIndex)
            .field(MAX_ITEMS_FIELD, maxItems)
            .endObject()
    }
}
