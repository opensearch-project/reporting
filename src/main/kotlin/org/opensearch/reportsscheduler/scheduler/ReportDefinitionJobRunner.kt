/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.search.SearchResponse
import org.opensearch.client.Client
import org.opensearch.client.node.NodeClient
import org.opensearch.cluster.service.ClusterService
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.index.query.QueryBuilders
import org.opensearch.jobscheduler.spi.JobExecutionContext
import org.opensearch.jobscheduler.spi.ScheduledJobParameter
import org.opensearch.jobscheduler.spi.ScheduledJobRunner
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.index.ReportInstancesIndex
import org.opensearch.reportsscheduler.model.ReportDefinitionDetails
import org.opensearch.reportsscheduler.model.ReportInstance
import org.opensearch.reportsscheduler.util.NotificationApiUtils.getNotificationConfigInfo
import org.opensearch.reportsscheduler.util.buildReportLink
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.reportsscheduler.util.sendNotificationWithHTML
import org.opensearch.search.builder.SearchSourceBuilder
import java.time.Instant

internal object ReportDefinitionJobRunner : ScheduledJobRunner {
    private val log by logger(ReportDefinitionJobRunner::class.java)
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var client: Client
    private lateinit var clusterService: ClusterService

    /**
     * Initialize the class
     * @param client The ES client
     * @param clusterService The ES cluster service
     */
    fun initialize(client: Client, clusterService: ClusterService) {
        this.client = client
        this.clusterService = clusterService
    }

    private suspend fun createNotification(
        configInfo: NotificationConfigInfo,
        reportDefinitionDetails: ReportDefinitionDetails,
        id: String,
        hits: Long?
    ) {
        val title: String = reportDefinitionDetails.reportDefinition.delivery!!.title
        val textMessage: String = reportDefinitionDetails.reportDefinition.delivery.textDescription
        val htmlMessage: String? = reportDefinitionDetails.reportDefinition.delivery.htmlDescription

        val urlDefinition: String =
            buildReportLink(reportDefinitionDetails.reportDefinition.source.origin, reportDefinitionDetails.tenant, id)

        val textWithURL: String =
            textMessage.replace("{{urlDefinition}}", urlDefinition).replace("{{hits}}", hits.toString())
        val htmlWithURL: String? =
            htmlMessage?.replace("{{urlDefinition}}", urlDefinition)?.replace("{{hits}}", hits.toString())

        log.debug("HTML message: $htmlMessage") // TODO remove
        configInfo.sendNotificationWithHTML(
            this.client,
            title,
            textWithURL,
            htmlWithURL
        )
    }

    override fun runJob(job: ScheduledJobParameter, context: JobExecutionContext) {
        if (job !is ReportDefinitionDetails) {
            log.warn("$LOG_PREFIX:job is not of type ReportDefinitionDetails:${job.javaClass.name}")
            throw IllegalArgumentException("job is not of type ReportDefinitionDetails:${job.javaClass.name}")
        }
        scope.launch {
            val reportDefinitionDetails: ReportDefinitionDetails = job
            val currentTime = Instant.now()
            val endTime = context.expectedExecutionTime
            val beginTime = endTime.minus(reportDefinitionDetails.reportDefinition.format.duration)
            val reportInstance = ReportInstance(
                context.jobId,
                currentTime,
                currentTime,
                beginTime,
                endTime,
                job.tenant,
                job.access,
                reportDefinitionDetails,
                ReportInstance.Status.Success
            ) // TODO: Revert to Scheduled when background job execution supported
            val id = ReportInstancesIndex.createReportInstance(reportInstance)
            if (id == null) {
                log.warn("$LOG_PREFIX:runJob-job creation failed for $reportInstance")
            } else {
                log.info("$LOG_PREFIX:runJob-created job:$id")

                // Wazuh - Make queries
                val builderSearchResponse: SearchSourceBuilder = SearchSourceBuilder()
                    .query(
                        QueryBuilders.boolQuery()
                            .must(
                                QueryBuilders.rangeQuery("timestamp")
                                    .gt(beginTime)
                                    .lte(currentTime)
                            )
                            .must(
                                QueryBuilders.matchQuery("agent.id", "001")
                            )
                    )
                val jobSearchRequest: SearchRequest =
                    SearchRequest().indices("wazuh-alerts-*").source(builderSearchResponse)
                val response: SearchResponse = client.search(jobSearchRequest).actionGet()

                val reportDefinitionId = reportDefinitionDetails.reportDefinition.delivery!!.configIds[0]
                val configInfo: NotificationConfigInfo? = getNotificationConfigInfo(
                    client as NodeClient,
                    reportDefinitionId
                )

                if (configInfo != null) {
                    createNotification(
                        configInfo,
                        reportDefinitionDetails,
                        id,
                        response.hits.totalHits?.value
                    )
                    log.info("Notification with id $id was sent.")
                } else {
                    log.error("NotificationConfigInfo with id $reportDefinitionId was not found.")
                }
            }
        }
    }
}
