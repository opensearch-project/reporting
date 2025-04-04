/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.model

import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.ToXContentObject
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import org.opensearch.jobscheduler.spi.schedule.Schedule
import org.opensearch.jobscheduler.spi.schedule.ScheduleParser
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.reportsscheduler.util.stringList
import java.time.Duration

/**
 * Report definition main data class.
 * <pre> JSON format
 * {@code
 * {
 *   "name":"name",
 *   "isEnabled":true,
 *   "source":{
 *      "description":"description",
 *      "type":"Dashboard", // Dashboard, Visualization, SavedSearch
 *      "origin":"http://localhost:5601",
 *      "id":"id"
 *   },
 *   "format":{
 *       "duration":"PT1H",
 *       "fileFormat":"Pdf", // Pdf, Png, Csv, Xlsx
 *       "limit":1000, // optional
 *       "header":"optional header",
 *       "footer":"optional footer"
 *   },
 *   "trigger":{
 *       "triggerType":"OnDemand", // Download, OnDemand, CronSchedule, IntervalSchedule
 *       "schedule":{ // required when triggerType is CronSchedule or IntervalSchedule
 *           "cron":{ // required when triggerType is CronSchedule
 *               "expression":"0 * * * *",
 *               "timezone":"PST"
 *           },
 *           "interval":{ // required when triggerType is IntervalSchedule
 *               "start_time":1603506908773,
 *               "period":10",
 *               "unit":"Minutes"
 *           }
 *       }
 *   },
 *   "delivery":{ // optional
 *       "recipients":["banantha@amazon.com"],
 *       "deliveryFormat":"LinkOnly", // LinkOnly, Attachment, Embedded
 *       "title":"title",
 *       "textDescription":"textDescription",
 *       "htmlDescription":"optional htmlDescription",
 *       "configIds":["optional_configIds"]
 *   }
 * }
 * }</pre>
 */

internal data class ReportDefinition(
    val name: String,
    val isEnabled: Boolean,
    val source: Source,
    val format: Format,
    val trigger: Trigger,
    val delivery: Delivery?
) : ToXContentObject {

    internal enum class SourceType { Dashboard, Visualization, SavedSearch, Notebook }
    internal enum class TriggerType { Download, OnDemand, CronSchedule, IntervalSchedule }
    internal enum class DeliveryFormat { LinkOnly, Attachment, Embedded }
    internal enum class FileFormat { Pdf, Png, Csv, Xlsx }

    internal companion object {
        private val log by logger(ReportDefinition::class.java)
        private const val NAME_TAG = "name"
        private const val IS_ENABLED_TAG = "isEnabled"
        private const val SOURCE_TAG = "source"
        private const val FORMAT_TAG = "format"
        private const val TRIGGER_TAG = "trigger"
        private const val DELIVERY_TAG = "delivery"

        /**
         * Parse the data from parser and create ReportDefinition object
         * @param parser data referenced at parser
         * @return created ReportDefinition object
         */
        fun parse(parser: XContentParser): ReportDefinition {
            var name: String? = null
            var isEnabled = false
            var source: Source? = null
            var format: Format? = null
            var trigger: Trigger? = null
            var delivery: Delivery? = null
            XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
            while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    NAME_TAG -> name = parser.text()
                    IS_ENABLED_TAG -> isEnabled = parser.booleanValue()
                    SOURCE_TAG -> source = Source.parse(parser)
                    FORMAT_TAG -> format = Format.parse(parser)
                    TRIGGER_TAG -> trigger = Trigger.parse(parser)
                    DELIVERY_TAG -> delivery = Delivery.parse(parser)
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:ReportDefinition Skipping Unknown field $fieldName")
                    }
                }
            }
            name ?: throw IllegalArgumentException("$NAME_TAG field absent")
            source ?: throw IllegalArgumentException("$SOURCE_TAG field absent")
            format ?: throw IllegalArgumentException("$FORMAT_TAG field absent")
            trigger ?: throw IllegalArgumentException("$TRIGGER_TAG field absent")
            return ReportDefinition(
                name,
                isEnabled,
                source,
                format,
                trigger,
                delivery
            )
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
        builder!!
        builder.startObject()
            .field(NAME_TAG, name)
            .field(IS_ENABLED_TAG, isEnabled)
        builder.field(SOURCE_TAG)
        source.toXContent(builder, ToXContent.EMPTY_PARAMS)
        builder.field(FORMAT_TAG)
        format.toXContent(builder, ToXContent.EMPTY_PARAMS)
        builder.field(TRIGGER_TAG)
        trigger.toXContent(builder, ToXContent.EMPTY_PARAMS)
        if (delivery != null) {
            builder.field(DELIVERY_TAG)
            delivery.toXContent(builder, ToXContent.EMPTY_PARAMS)
        }
        builder.endObject()
        return builder
    }

    /**
     * Report definition source data class
     */
    internal data class Source(
        val description: String,
        val type: SourceType,
        val origin: String,
        val id: String
    ) : ToXContentObject {
        internal companion object {
            private const val DESCRIPTION_TAG = "description"
            private const val TYPE_TAG = "type"
            private const val ORIGIN_TAG = "origin"
            private const val ID_TAG = "id"

            /**
             * Parse the data from parser and create Source object
             * @param parser data referenced at parser
             * @return created Source object
             */
            fun parse(parser: XContentParser): Source {
                var description: String? = null
                var type: SourceType? = null
                var origin: String? = null
                var id: String? = null
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
                while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                    val fieldName = parser.currentName()
                    parser.nextToken()
                    when (fieldName) {
                        DESCRIPTION_TAG -> description = parser.text()
                        TYPE_TAG -> type = SourceType.valueOf(parser.text())
                        ORIGIN_TAG -> origin = parser.text()
                        ID_TAG -> id = parser.text()
                        else -> {
                            parser.skipChildren()
                            log.info("$LOG_PREFIX:Source Skipping Unknown field $fieldName")
                        }
                    }
                }
                description ?: throw IllegalArgumentException("$DESCRIPTION_TAG field absent")
                type ?: throw IllegalArgumentException("$TYPE_TAG field absent")
                origin ?: throw IllegalArgumentException("$ORIGIN_TAG field absent")
                id ?: throw IllegalArgumentException("$ID_TAG field absent")
                return Source(
                    description,
                    type,
                    origin,
                    id
                )
            }
        }

        /**
         * {@inheritDoc}
         */
        override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
            builder!!
            builder.startObject()
                .field(DESCRIPTION_TAG, description)
                .field(TYPE_TAG, type.name)
                .field(ORIGIN_TAG, origin)
                .field(ID_TAG, id)
                .endObject()
            return builder
        }
    }

    /**
     * Report definition format data class
     */
    internal data class Format(
        val duration: Duration,
        val fileFormat: FileFormat,
        val limit: Int?,
        val header: String?,
        val footer: String?,
        val timeFrom: String?,
        val timeTo: String?
    ) : ToXContentObject {
        internal companion object {
            private const val DURATION_TAG = "duration"
            private const val FILE_FORMAT_TAG = "fileFormat"
            private const val LIMIT_TAG = "limit"
            private const val HEADER_TAG = "header"
            private const val FOOTER_TAG = "footer"
            private const val TIME_FROM_TAG = "timeFrom"
            private const val TIME_TO_TAG = "timeTo"

            /**
             * Parse the data from parser and create Format object
             * @param parser data referenced at parser
             * @return created Format object
             */
            fun parse(parser: XContentParser): Format {
                var durationSeconds: Duration? = null
                var fileFormat: FileFormat? = null
                var limit: Int? = null
                var header: String? = null
                var footer: String? = null
                var timeFrom: String? = null
                var timeTo: String? = null
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
                while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                    val fieldName = parser.currentName()
                    parser.nextToken()
                    when (fieldName) {
                        DURATION_TAG -> durationSeconds = Duration.parse(parser.text())
                        FILE_FORMAT_TAG -> fileFormat = FileFormat.valueOf(parser.text())
                        LIMIT_TAG -> limit = parser.intValue()
                        HEADER_TAG -> header = parser.textOrNull()
                        FOOTER_TAG -> footer = parser.textOrNull()
                        TIME_FROM_TAG -> timeFrom = parser.textOrNull()
                        TIME_TO_TAG -> timeTo = parser.textOrNull()
                        else -> {
                            parser.skipChildren()
                            log.info("$LOG_PREFIX:Format Skipping Unknown field $fieldName")
                        }
                    }
                }
                durationSeconds ?: throw IllegalArgumentException("$DURATION_TAG field absent")
                fileFormat ?: throw IllegalArgumentException("$FILE_FORMAT_TAG field absent")
                return Format(
                    durationSeconds,
                    fileFormat,
                    limit,
                    header,
                    footer,
                    timeFrom,
                    timeTo
                )
            }
        }

        /**
         * {@inheritDoc}
         */
        override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
            builder!!
            builder.startObject()
                .field(DURATION_TAG, duration.toString())
                .field(FILE_FORMAT_TAG, fileFormat.name)
            if (limit != null) builder.field(LIMIT_TAG, limit)
            if (header != null) builder.field(HEADER_TAG, header)
            if (footer != null) builder.field(FOOTER_TAG, footer)
            builder.field(TIME_FROM_TAG, timeFrom)
            builder.field(TIME_TO_TAG, timeTo)
            builder.endObject()
            return builder
        }
    }

    /**
     * Report definition trigger data class
     */
    internal data class Trigger(
        val triggerType: TriggerType,
        val schedule: Schedule?
    ) : ToXContentObject {
        internal companion object {
            private const val TRIGGER_TYPE_TAG = "triggerType"
            private const val SCHEDULE_TAG = "schedule"

            /**
             * Parse the data from parser and create Trigger object
             * @param parser data referenced at parser
             * @return created Trigger object
             */
            fun parse(parser: XContentParser): Trigger {
                var triggerType: TriggerType? = null
                var schedule: Schedule? = null
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
                while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                    val fieldName = parser.currentName()
                    parser.nextToken()
                    when (fieldName) {
                        TRIGGER_TYPE_TAG -> triggerType = TriggerType.valueOf(parser.text())
                        SCHEDULE_TAG -> schedule = ScheduleParser.parse(parser)
                        else -> log.info("$LOG_PREFIX: Trigger Skipping Unknown field $fieldName")
                    }
                }
                triggerType ?: throw IllegalArgumentException("$TRIGGER_TYPE_TAG field absent")
                if (isScheduleType(triggerType)) {
                    schedule ?: throw IllegalArgumentException("$SCHEDULE_TAG field absent")
                }
                return Trigger(triggerType, schedule)
            }

            fun isScheduleType(triggerType: TriggerType): Boolean {
                return (triggerType == TriggerType.CronSchedule || triggerType == TriggerType.IntervalSchedule)
            }
        }

        /**
         * {@inheritDoc}
         */
        override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
            builder!!
            builder.startObject()
                .field(TRIGGER_TYPE_TAG, triggerType)
            if (isScheduleType(triggerType)) {
                builder.field(SCHEDULE_TAG)
                schedule!!.toXContent(builder, ToXContent.EMPTY_PARAMS)
            }
            builder.endObject()
            return builder
        }
    }

    /**
     * Report definition delivery data class
     */
    internal data class Delivery(
        val title: String,
        val textDescription: String,
        val htmlDescription: String?,
        val configIds: List<String>
    ) : ToXContentObject {
        internal companion object {
            private const val TITLE_TAG = "title"
            private const val TEXT_DESCRIPTION_TAG = "textDescription"
            private const val HTML_DESCRIPTION_TAG = "htmlDescription"
            private const val CONFIG_IDS_TAG = "configIds"

            /**
             * Parse the data from parser and create Delivery object
             * @param parser data referenced at parser
             * @return created Delivery object
             */
            fun parse(parser: XContentParser): Delivery {
                var title: String? = null
                var textDescription: String? = null
                var htmlDescription: String? = null
                var configIds: List<String> = listOf()
                XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser)
                while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                    val fieldName = parser.currentName()
                    parser.nextToken()
                    when (fieldName) {
                        TITLE_TAG -> title = parser.text()
                        TEXT_DESCRIPTION_TAG -> textDescription = parser.text()
                        HTML_DESCRIPTION_TAG -> htmlDescription = parser.textOrNull()
                        CONFIG_IDS_TAG -> configIds = parser.stringList()
                        else -> log.info("$LOG_PREFIX: Delivery Unknown field $fieldName")
                    }
                }
                title ?: throw IllegalArgumentException("$TITLE_TAG field absent")
                textDescription ?: throw IllegalArgumentException("$TEXT_DESCRIPTION_TAG field absent")
                return Delivery(
                    title,
                    textDescription,
                    htmlDescription,
                    configIds
                )
            }
        }

        /**
         * {@inheritDoc}
         */
        override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
            builder!!
            builder.startObject()
                .field(TITLE_TAG, title)
                .field(TEXT_DESCRIPTION_TAG, textDescription)
            if (htmlDescription != null) {
                builder.field(HTML_DESCRIPTION_TAG, htmlDescription)
            }
            builder.field(CONFIG_IDS_TAG, configIds)
            builder.endObject()
            return builder
        }
    }
}
