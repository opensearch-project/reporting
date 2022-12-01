/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.scheduler

import org.opensearch.common.xcontent.XContentParser
import org.opensearch.jobscheduler.spi.JobDocVersion
import org.opensearch.jobscheduler.spi.ScheduledJobParameter
import org.opensearch.jobscheduler.spi.ScheduledJobParser
import org.opensearch.reportsscheduler.model.ReportDefinitionDetails

internal object ReportDefinitionJobParser : ScheduledJobParser {
    /**
     * {@inheritDoc}
     */
    override fun parse(xContentParser: XContentParser, id: String, jobDocVersion: JobDocVersion): ScheduledJobParameter {
        xContentParser.nextToken()
        return ReportDefinitionDetails.parse(xContentParser, id)
    }
}
