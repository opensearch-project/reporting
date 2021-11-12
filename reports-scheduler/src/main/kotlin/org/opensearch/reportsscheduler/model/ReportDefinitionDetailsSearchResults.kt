/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_LIST_FIELD
import org.opensearch.action.search.SearchResponse
import org.opensearch.common.xcontent.XContentParser

/**
 * ReportDefinitions search results
 */
internal class ReportDefinitionDetailsSearchResults : SearchResults<ReportDefinitionDetails> {
    constructor(parser: XContentParser) : super(parser, REPORT_DEFINITION_LIST_FIELD)

    constructor(from: Long, response: SearchResponse) : super(from, response, REPORT_DEFINITION_LIST_FIELD)

    /**
     * {@inheritDoc}
     */
    override fun parseItem(parser: XContentParser, useId: String?): ReportDefinitionDetails {
        return ReportDefinitionDetails.parse(parser, useId)
    }
}
