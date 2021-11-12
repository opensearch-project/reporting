/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.model.RestTag.REPORT_INSTANCE_LIST_FIELD
import org.apache.lucene.search.TotalHits
import org.opensearch.action.search.SearchResponse
import org.opensearch.common.xcontent.XContentParser

/**
 * ReportInstances search results
 */
internal class ReportInstanceSearchResults : SearchResults<ReportInstance> {
    constructor(
        startIndex: Long,
        totalHits: Long,
        totalHitRelation: TotalHits.Relation,
        objectList: List<ReportInstance>
    ) : super(startIndex, totalHits, totalHitRelation, objectList, REPORT_INSTANCE_LIST_FIELD)

    constructor(parser: XContentParser) : super(parser, REPORT_INSTANCE_LIST_FIELD)

    constructor(from: Long, response: SearchResponse) : super(from, response, REPORT_INSTANCE_LIST_FIELD)

    /**
     * {@inheritDoc}
     */
    override fun parseItem(parser: XContentParser, useId: String?): ReportInstance {
        return ReportInstance.parse(parser, useId)
    }
}
