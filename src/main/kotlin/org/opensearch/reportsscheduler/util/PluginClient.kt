/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionType
import org.opensearch.action.support.ContextPreservingActionListener
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.identity.PluginSubject
import org.opensearch.transport.client.Client
import org.opensearch.transport.client.FilterClient
import java.util.concurrent.atomic.AtomicReference

/**
 * A client that runs transport actions as this plugin's system subject.
 *
 * @param delegate the client whose actions this wraps
 */
class PluginClient(delegate: Client) : FilterClient(delegate) {

    // Set once, from assignSubject, on a different thread from the actions that read it.
    private val subject = AtomicReference<PluginSubject?>()

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(PluginClient::class.java)
    }

    /** Wraps a client whose actions run as the given subject. */
    constructor(delegate: Client, subject: PluginSubject) : this(delegate) {
        setSubject(subject)
    }

    /**
     * Assigns the subject every action runs as. It can be assigned once; assigning the same subject again is a no-op.
     *
     * @param subject the plugin's system subject, normally the one passed to assignSubject
     * @throws IllegalStateException if a different subject is already assigned
     */
    fun setSubject(subject: PluginSubject) {
        check(this.subject.compareAndSet(null, subject) || this.subject.get() === subject) {
            "PluginClient already has a subject assigned."
        }
    }

    /** Not supported, because a plugin subject has no meaning on another cluster. */
    override fun getRemoteClusterClient(clusterAlias: String): Client =
        throw UnsupportedOperationException("PluginClient cannot run actions on a remote cluster.")

    /**
     * Runs the action as the assigned subject, notifying the listener with the caller's thread context in place.
     *
     * @throws IllegalStateException if no subject has been assigned yet, which is a wiring error
     */
    override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
        action: ActionType<Response>,
        request: Request,
        listener: ActionListener<Response>
    ) {
        val currentSubject = subject.get() ?: error("PluginClient is not initialized with a subject.")
        // Captured before runAs stashes the context; core restores it around the callback, then puts
        // the responding thread's own context back.
        val callerContext = ContextPreservingActionListener.wrapPreservingContext(listener, threadPool().threadContext)
        currentSubject.runAs<RuntimeException> {
            LOGGER.debug("Running transport action as subject: {}", currentSubject.principal.name)
            super.doExecute(action, request, callerContext)
        }
    }
}
