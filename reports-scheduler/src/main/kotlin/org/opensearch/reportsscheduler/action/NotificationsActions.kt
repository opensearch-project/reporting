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

package org.opensearch.reportsscheduler.action

import org.opensearch.action.ActionListener
import org.opensearch.client.Client
import org.opensearch.client.node.NodeClient
import org.opensearch.cluster.service.ClusterService
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.Feature
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.model.CreateReportDefinitionResponse
import org.opensearch.reportsscheduler.model.ReportDefinition
import org.opensearch.reportsscheduler.util.SecureIndexClient
import org.opensearch.reportsscheduler.util.logger

/**
 * Report definitions index operation actions.
 */
internal object NotificationsActions {
    private val log by logger(NotificationsActions::class.java)

    private lateinit var client: NodeClient

    /**
     * Initialize the class
     * @param client NodeClient for transport call
     */
    fun initialize(client: NodeClient) {
        this.client = client
    }

    /**
     * Send notifications based on delivery parameter
     * @param delivery [ReportDefinition.Delivery] object
     * @return [CreateReportDefinitionResponse]
     */
    fun send(delivery: ReportDefinition.Delivery, referenceId: String) {
        log.info("$LOG_PREFIX:NotificationsActions-send")
        NotificationsPluginInterface.sendNotification(
            client,
            EventSource(delivery.title, referenceId, Feature.REPORTS, SeverityType.INFO),
            ChannelMessage(delivery.textDescription, delivery.htmlDescription, null),
            delivery.configIds,
            object : ActionListener<SendNotificationResponse> {
                override fun onResponse(p0: SendNotificationResponse) {
                    log.info("$LOG_PREFIX:NotificationsActions-send:${p0.notificationId}")
                }

                override fun onFailure(p0: java.lang.Exception) {
                    log.error("$LOG_PREFIX:NotificationsActions-send error:$p0")
                }
            }
        )
    }
}
