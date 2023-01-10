/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.reportsscheduler.util.createJsonParser
import java.io.IOException

/**
 * Get all report instances info response.
 * <pre> JSON format
 * {@code
 * {
 *   "startIndex":"0",
 *   "totalHits":"100",
 *   "totalHitRelation":"eq",
 *   "reportInstanceList":[
 *      // refer [org.opensearch.reportsscheduler.model.ReportInstance]
 *   ]
 * }
 * }</pre>
 */
internal class GetAllReportInstancesResponse : BaseResponse {
    val reportInstanceList: ReportInstanceSearchResults
    private val filterSensitiveInfo: Boolean

    constructor(reportInstanceList: ReportInstanceSearchResults, filterSensitiveInfo: Boolean) : super() {
        this.reportInstanceList = reportInstanceList
        this.filterSensitiveInfo = filterSensitiveInfo
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [GetAllReportInstancesResponse] object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser) : super() {
        this.reportInstanceList = ReportInstanceSearchResults(parser)
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
        return reportInstanceList.toXContent(builder, xContentParams)
    }
}
