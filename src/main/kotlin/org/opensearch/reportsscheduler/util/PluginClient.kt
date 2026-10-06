/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionType
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.identity.Subject
import org.opensearch.transport.client.Client
import org.opensearch.transport.client.FilterClient

/**
 * A special client for executing transport actions as this plugin's system subject.
 */
class PluginClient : FilterClient {

    // Assigned from IdentityAwarePlugin.assignSubject, which runs on a different thread than the
    // transport actions that read it.
    @Volatile
    private var subject: Subject? = null

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(PluginClient::class.java)
    }

    constructor(delegate: Client) : super(delegate)

    constructor(delegate: Client, subject: Subject) : super(delegate) {
        this.subject = subject
    }

    fun setSubject(subject: Subject) {
        this.subject = subject
    }

    @Suppress("TooGenericExceptionCaught")
    override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
        action: ActionType<Response>,
        request: Request,
        listener: ActionListener<Response>
    ) {
        val currentSubject = subject
            ?: error("PluginClient is not initialized.")

        // Saves the caller's context so the listener can be given it back. runAs switches the
        // context itself and restores it when its body returns, so this exists for the listener,
        // which runs later and on a thread that never carried the caller's context.
        val storedContext = threadPool().threadContext.newStoredContext(false)

        try {
            currentSubject.runAs<Exception> {
                LOGGER.debug("Running transport action with subject: {}", currentSubject.principal.name)

                super.doExecute(action, request, ActionListener.runBefore(listener) { storedContext.restore() })
            }
        } catch (exception: Exception) {
            // Reported through the listener rather than thrown, so a caller that only waits on the
            // listener is not left waiting forever.
            storedContext.close()
            listener.onFailure(exception)
        }
    }
}
