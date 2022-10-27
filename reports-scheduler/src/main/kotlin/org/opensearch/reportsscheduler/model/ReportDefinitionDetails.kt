/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContent.EMPTY_PARAMS
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.jobscheduler.spi.ScheduledJobParameter
import org.opensearch.jobscheduler.spi.schedule.Schedule
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.model.ReportDefinition.TriggerType
import org.opensearch.reportsscheduler.model.RestTag.ACCESS_LIST_FIELD
import org.opensearch.reportsscheduler.model.RestTag.CREATED_TIME_FIELD
import org.opensearch.reportsscheduler.model.RestTag.ID_FIELD
import org.opensearch.reportsscheduler.model.RestTag.REPORT_DEFINITION_FIELD
import org.opensearch.reportsscheduler.model.RestTag.TENANT_FIELD
import org.opensearch.reportsscheduler.model.RestTag.UPDATED_TIME_FIELD
import org.opensearch.reportsscheduler.security.UserAccessManager.DEFAULT_TENANT
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.reportsscheduler.util.stringList
import java.time.Instant

/**
 * Wrapper data class over ReportDefinition to add metadata
 * <pre> JSON format
 * {@code
 * {
 *   "id":"id",
 *   "lastUpdatedTimeMs":1603506908773,
 *   "createdTimeMs":1603506908773,
 *   "tenant":"__user__",
 *   "access":["User:user", "Role:sample_role", "BERole:sample_backend_role"]
 *   "reportDefinition":{
 *      // refer [org.opensearch.reportsscheduler.model.ReportDefinition]
 *   }
 * }
 * }</pre>
 */
internal data class ReportDefinitionDetails(
    val id: String,
    val updatedTime: Instant,
    val createdTime: Instant,
    val tenant: String,
    val access: List<String>,
    val reportDefinition: ReportDefinition
) : ScheduledJobParameter {
    internal companion object {
        private val log by logger(ReportDefinitionDetails::class.java)

        /**
         * Parse the data from parser and create ReportDefinitionDetails object
         * @param parser data referenced at parser
         * @param useId use this id if not available in the json
         * @return created ReportDefinitionDetails object
         */
        fun parse(parser: XContentParser, useId: String? = null): ReportDefinitionDetails {
            var id: String? = useId
            var updatedTime: Instant? = null
            var createdTime: Instant? = null
            var tenant: String? = null
            var access: List<String> = listOf()
            var reportDefinition: ReportDefinition? = null
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
            while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    ID_FIELD -> id = parser.text()
                    UPDATED_TIME_FIELD -> updatedTime = Instant.ofEpochMilli(parser.longValue())
                    CREATED_TIME_FIELD -> createdTime = Instant.ofEpochMilli(parser.longValue())
                    TENANT_FIELD -> tenant = parser.text()
                    ACCESS_LIST_FIELD -> access = parser.stringList()
                    REPORT_DEFINITION_FIELD -> reportDefinition = ReportDefinition.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:ReportDefinitionDetails Skipping Unknown field $fieldName")
                    }
                }
            }
            id ?: throw IllegalArgumentException("$ID_FIELD field absent")
            updatedTime ?: throw IllegalArgumentException("$UPDATED_TIME_FIELD field absent")
            createdTime ?: throw IllegalArgumentException("$CREATED_TIME_FIELD field absent")
            tenant = tenant ?: DEFAULT_TENANT
            reportDefinition ?: throw IllegalArgumentException("$REPORT_DEFINITION_FIELD field absent")
            return ReportDefinitionDetails(
                id,
                updatedTime,
                createdTime,
                tenant,
                access,
                reportDefinition
            )
        }
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @param params XContent parameters
     * @return created XContentBuilder object
     */
    fun toXContent(params: ToXContent.Params = EMPTY_PARAMS): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * {ref toXContent}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
        if (params?.paramAsBoolean(ID_FIELD, false) == true) {
            builder.field(ID_FIELD, id)
        }
        builder.field(UPDATED_TIME_FIELD, updatedTime.toEpochMilli())
            .field(CREATED_TIME_FIELD, createdTime.toEpochMilli())
            .field(TENANT_FIELD, tenant)
        if (params?.paramAsBoolean(ACCESS_LIST_FIELD, true) == true && access.isNotEmpty()) {
            builder.field(ACCESS_LIST_FIELD, access)
        }
        builder.field(REPORT_DEFINITION_FIELD)
        reportDefinition.toXContent(builder, params)
        builder.endObject()
        return builder
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return reportDefinition.name
    }

    /**
     * {@inheritDoc}
     */
    override fun getLastUpdateTime(): Instant {
        return updatedTime
    }

    /**
     * {@inheritDoc}
     */
    override fun getEnabledTime(): Instant {
        return createdTime
    }

    override fun getSchedule(): Schedule {
        return reportDefinition.trigger.schedule!!
    }

    override fun isEnabled(): Boolean {
        val trigger = reportDefinition.trigger
        return (
            reportDefinition.isEnabled &&
                (reportDefinition.trigger.schedule != null) &&
                (trigger.triggerType == TriggerType.IntervalSchedule || trigger.triggerType == TriggerType.CronSchedule)
            )
    }
}
