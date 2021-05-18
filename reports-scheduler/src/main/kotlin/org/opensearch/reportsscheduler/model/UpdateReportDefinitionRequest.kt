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
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_FIELD
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_ID_FIELD
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
 * Report Definition-update request.
 * reportDefinitionId is from request query params
 * <pre> JSON format
 * {@code
 * {
 *   "reportDefinitionId":"reportDefinitionId",
 *   "reportDefinition":{
 *      // refer [org.opensearch.reportsscheduler.model.ReportDefinition]
 *   }
 * }
 * }</pre>
 */
internal class UpdateReportDefinitionRequest : ActionRequest, ToXContentObject {
    val reportDefinitionId: String
    val reportDefinition: ReportDefinition

    companion object {
        private val log by logger(CreateReportDefinitionRequest::class.java)
    }

    constructor(reportDefinitionId: String, reportDefinition: ReportDefinition) : super() {
        this.reportDefinitionId = reportDefinitionId
        this.reportDefinition = reportDefinition
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [UpdateReportDefinitionRequest] object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser, useReportDefinitionId: String? = null) : super() {
        var reportDefinitionId: String? = useReportDefinitionId
        var reportDefinition: ReportDefinition? = null
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
        while (Token.END_OBJECT != parser.nextToken()) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                REPORT_DEFINITION_ID_FIELD -> reportDefinitionId = parser.text()
                REPORT_DEFINITION_FIELD -> reportDefinition = ReportDefinition.parse(parser)
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                }
            }
        }
        reportDefinitionId ?: run {
            Metrics.REPORT_DEFINITION_UPDATE_USER_ERROR_INVALID_REPORT_DEF_ID.counter.increment()
            throw IllegalArgumentException("$REPORT_DEFINITION_ID_FIELD field absent")
        }
        reportDefinition ?: run {
            Metrics.REPORT_DEFINITION_UPDATE_USER_ERROR_INVALID_REPORT_DEF.counter.increment()
            throw IllegalArgumentException("$REPORT_DEFINITION_FIELD field absent")
        }
        this.reportDefinitionId = reportDefinitionId
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
            .field(REPORT_DEFINITION_ID_FIELD, reportDefinitionId)
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
