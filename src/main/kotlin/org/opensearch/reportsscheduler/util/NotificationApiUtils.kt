/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.apache.logging.log4j.LogManager
import org.opensearch.OpenSearchSecurityException
import org.opensearch.OpenSearchStatusException
import org.opensearch.client.Client
import org.opensearch.client.node.NodeClient
import org.opensearch.commons.notifications.NotificationsPluginInterface
import org.opensearch.commons.notifications.action.GetNotificationConfigRequest
import org.opensearch.commons.notifications.action.GetNotificationConfigResponse
import org.opensearch.commons.notifications.action.SendNotificationResponse
import org.opensearch.commons.notifications.model.ChannelMessage
import org.opensearch.commons.notifications.model.EventSource
import org.opensearch.commons.notifications.model.NotificationConfigInfo
import org.opensearch.commons.notifications.model.SeverityType
import org.opensearch.core.action.ActionListener
import org.opensearch.core.rest.RestStatus
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object NotificationApiUtils {

    private val logger = LogManager.getLogger(NotificationApiUtils::class)

    /**
     * Gets a NotificationConfigInfo object by ID if it exists.
     */
    suspend fun getNotificationConfigInfo(client: NodeClient, id: String): NotificationConfigInfo? {
        return try {
            val res: GetNotificationConfigResponse =
                getNotificationConfig(client, GetNotificationConfigRequest(setOf(id)))
            res.searchResult.objectList.firstOrNull()
        } catch (e: OpenSearchSecurityException) {
            throw e
        } catch (e: OpenSearchStatusException) {
            if (e.status() == RestStatus.NOT_FOUND) {
                logger.debug("Notification config [$id] was not found")
            }
            null
        }
    }

    private suspend fun getNotificationConfig(
        client: NodeClient,
        getNotificationConfigRequest: GetNotificationConfigRequest
    ): GetNotificationConfigResponse {
        val getNotificationConfigResponse: GetNotificationConfigResponse =
            NotificationsPluginInterface.suspendUntil {
                this.getNotificationConfig(
                    client,
                    getNotificationConfigRequest,
                    it
                )
            }
        return getNotificationConfigResponse
    }
}

/**
 * Extension function for publishing a notification to a channel in the Notification plugin.
 */
suspend fun NotificationConfigInfo.sendNotificationWithHTML(
    client: Client,
    title: String,
    compiledMessage: String,
    compiledMessageHTML: String?
): String {
    val config = this
    val res: SendNotificationResponse = NotificationsPluginInterface.suspendUntil {
        this.sendNotification(
            (client as NodeClient),
            EventSource(title, config.configId, SeverityType.INFO),
            ChannelMessage(compiledMessage, compiledMessageHTML, null),
            listOf(config.configId),
            it
        )
    }
    validateResponseStatus(res.getStatus(), res.notificationEvent.toString())
    return res.notificationEvent.toString()
}

/**
 * Converts [NotificationsPluginInterface] methods that take a callback into a kotlin suspending function.
 *
 * @param block - a block of code that is passed an [ActionListener] that should be passed to the NotificationsPluginInterface API.
 */
suspend fun <T> NotificationsPluginInterface.suspendUntil(block: NotificationsPluginInterface.(ActionListener<T>) -> Unit): T =
    suspendCoroutine { cont ->
        block(object : ActionListener<T> {
            override fun onResponse(response: T) = cont.resume(response)

            override fun onFailure(e: Exception) = cont.resumeWithException(e)
        })
    }

/**
 * All valid response statuses.
 */
private val VALID_RESPONSE_STATUS = setOf(
    RestStatus.OK.status,
    RestStatus.CREATED.status,
    RestStatus.ACCEPTED.status,
    RestStatus.NON_AUTHORITATIVE_INFORMATION.status,
    RestStatus.NO_CONTENT.status,
    RestStatus.RESET_CONTENT.status,
    RestStatus.PARTIAL_CONTENT.status,
    RestStatus.MULTI_STATUS.status
)

@Throws(OpenSearchStatusException::class)
fun validateResponseStatus(restStatus: RestStatus, responseContent: String) {
    if (!VALID_RESPONSE_STATUS.contains(restStatus.status)) {
        throw OpenSearchStatusException("Failed: $responseContent", restStatus)
    }
}
