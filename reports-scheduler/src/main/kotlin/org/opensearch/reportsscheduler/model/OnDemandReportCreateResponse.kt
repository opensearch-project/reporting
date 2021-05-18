/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.model.RestTag.REPORT_INSTANCE_FIELD
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
 * Report-create response for on-demand report definition.
 * <pre> JSON format
 * {@code
 * {
 *   "reportInstance":{
 *      // refer [org.opensearch.reportsscheduler.model.ReportInstance]
 *   }
 * }
 * }</pre>
 */
internal class OnDemandReportCreateResponse : BaseResponse {
    val reportInstance: ReportInstance
    private val filterSensitiveInfo: Boolean

    companion object {
        private val log by logger(OnDemandReportCreateResponse::class.java)
    }

    constructor(reportInstance: ReportInstance, filterSensitiveInfo: Boolean) : super() {
        this.reportInstance = reportInstance
        this.filterSensitiveInfo = filterSensitiveInfo
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [OnDemandReportCreateResponse] object
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
