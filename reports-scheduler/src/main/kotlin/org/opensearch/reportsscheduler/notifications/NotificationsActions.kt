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

package org.opensearch.reportsscheduler.notifications

import org.opensearch.OpenSearchException
import org.opensearch.action.ActionListener
import org.opensearch.client.node.NodeClient
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.commons.ConfigConstants
import org.opensearch.commons.notifications.NotificationConstants.FEATURE_REPORTS
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.CreateReportDefinitionResponse
import org.opensearch.reportsscheduler.model.ReportDefinition
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
     * @param referenceId [String] object
     * @return [CreateReportDefinitionResponse]
     */
    fun send(delivery: ReportDefinition.Delivery, referenceId: String): SendNotificationResponse? {
        return send(delivery, referenceId, "")
    }

    /**
     * Send notifications based on delivery parameter
     * @param delivery [ReportDefinition.Delivery] object
     * @param referenceId [String] object
     * @param userStr [String] object,
     * @return [CreateReportDefinitionResponse]
     */
    fun send(delivery: ReportDefinition.Delivery, referenceId: String, userStr: String?): SendNotificationResponse? {
        if (userStr.isNullOrEmpty()) {
            return sendNotificationHelper(delivery, referenceId)
        }

        var sendNotificationResponse: SendNotificationResponse? = null
        client.threadPool().threadContext.stashContext().use {
            client.threadPool().threadContext.putTransient(
                ConfigConstants.OPENSEARCH_SECURITY_USER_INFO_THREAD_CONTEXT,
                userStr
            )
            sendNotificationResponse = sendNotificationHelper(delivery, referenceId)
        }
        return sendNotificationResponse
    }

    @Suppress("TooGenericExceptionCaught")
    private fun sendNotificationHelper(
        delivery: ReportDefinition.Delivery,
        referenceId: String
    ): SendNotificationResponse? {
        log.info("$LOG_PREFIX:NotificationsActions-send")
        var sendNotificationResponse: SendNotificationResponse? = null
        Metrics.REPORT_NOTIFICATIONS_TOTAL.counter.increment()
        try {
            NotificationsPluginInterface.sendNotification(
                client,
                EventSource(delivery.title, referenceId, FEATURE_REPORTS, SeverityType.INFO),
                ChannelMessage(delivery.textDescription, delivery.htmlDescription, null),
                delivery.configIds,
                object : ActionListener<SendNotificationResponse> {
                    override fun onResponse(response: SendNotificationResponse) {
                        sendNotificationResponse = response
                        log.info("$LOG_PREFIX:NotificationsActions-send:$sendNotificationResponse")
                    }

                    override fun onFailure(exception: Exception) {
                        log.error("$LOG_PREFIX:NotificationsActions-send Error:$exception")
                        throw exception
                    }
                }
            )
        } catch (e: Exception) {
            Metrics.REPORT_NOTIFICATIONS_ERROR.counter.increment()
            val isMissingNotificationPlugin = e.message?.contains("failed to find action") ?: false
            if (isMissingNotificationPlugin) {
                throw OpenSearchException(
                    "Notification plugin is not installed. Please install the Notification plugin.",
                    e
                )
            } else {
                throw e
            }
        }
        return sendNotificationResponse
    }

    /**
     * Executes the given [block] function on this resource and then closes it down correctly whether an exception
     * is thrown or not.
     *
     * In case if the resource is being closed due to an exception occurred in [block], and the closing also fails with an exception,
     * the latter is added to the [suppressed][java.lang.Throwable.addSuppressed] exceptions of the former.
     *
     * @param block a function to process this [AutoCloseable] resource.
     * @return the result of [block] function invoked on this resource.
     */
    @Suppress("TooGenericExceptionCaught")
    private inline fun <T : ThreadContext.StoredContext, R> T.use(block: (T) -> R): R {
        var exception: Throwable? = null
        try {
            return block(this)
        } catch (e: Throwable) {
            exception = e
            throw e
        } finally {
            closeFinally(exception)
        }
    }

    /**
     * Closes this [AutoCloseable], suppressing possible exception or error thrown by [AutoCloseable.close] function when
     * it's being closed due to some other [cause] exception occurred.
     *
     * The suppressed exception is added to the list of suppressed exceptions of [cause] exception.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun ThreadContext.StoredContext.closeFinally(cause: Throwable?) = when (cause) {
        null -> close()
        else -> try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
    }
}
