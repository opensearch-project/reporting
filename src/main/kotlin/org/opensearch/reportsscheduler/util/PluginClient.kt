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

    @Suppress("UNCHECKED_CAST")
    override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
        action: ActionType<Response>,
        request: Request,
        listener: ActionListener<Response>
    ) {
        val currentSubject = subject
            ?: error("PluginClient is not initialized.")

        val storedContext = threadPool().threadContext.newStoredContext(false)

        try {
            currentSubject.runAs<Exception> {
                LOGGER.info("Running transport action with subject: {}", currentSubject.principal.name)

                // Wrap listener to restore context before invocation
                val wrappedListener = ActionListener.runBefore(listener) { storedContext.restore() }

                super.doExecute(action, request, wrappedListener)
            }
        } finally {
            storedContext.close()
        }
    }
}
