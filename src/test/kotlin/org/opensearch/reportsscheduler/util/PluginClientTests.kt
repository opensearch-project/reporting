/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ActionType
import org.opensearch.common.CheckedRunnable
import org.opensearch.common.settings.Settings
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.identity.NamedPrincipal
import org.opensearch.identity.Subject
import org.opensearch.threadpool.TestThreadPool
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.client.support.AbstractClient
import java.security.Principal
import java.util.concurrent.TimeUnit

internal class PluginClientTests {

    companion object {
        private const val CALLER_HEADER = "test-caller-header"
        private val TEST_ACTION = ActionType("cluster:admin/opendistro/reports/test", Writeable.Reader { StubResponse() })
    }

    private lateinit var threadPool: ThreadPool
    private lateinit var delegate: RecordingClient

    @BeforeEach
    fun setUp() {
        threadPool = TestThreadPool(PluginClientTests::class.java.name)
        delegate = RecordingClient(threadPool)
    }

    @AfterEach
    fun tearDown() {
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS)
    }

    @Test
    fun `runs the delegate under the subject and hands the calling thread back its context`() {
        val pluginClient = PluginClient(delegate, StashingSubject(threadPool))
        threadPool.threadContext.putHeader(CALLER_HEADER, "caller")

        pluginClient.execute(TEST_ACTION, StubRequest(), RecordingListener(threadPool))

        assertTrue(delegate.executed)
        assertNull(delegate.headerSeen)
        assertEquals("caller", threadPool.threadContext.getHeader(CALLER_HEADER))
    }

    @Test
    fun `restores the caller context before the listener is invoked`() {
        val pluginClient = PluginClient(delegate, StashingSubject(threadPool))
        val listener = RecordingListener(threadPool)
        threadPool.threadContext.putHeader(CALLER_HEADER, "caller")

        pluginClient.execute(TEST_ACTION, StubRequest(), listener)

        // A transport response arrives on a pooled thread that never carried the caller's context.
        val stashed = threadPool.threadContext.stashContext()
        try {
            assertNull(threadPool.threadContext.getHeader(CALLER_HEADER))
            delegate.capturedListener().onResponse(StubResponse())
        } finally {
            stashed.close()
        }

        assertTrue(listener.invoked)
        assertEquals("caller", listener.headerOnInvocation)
    }

    @Test
    fun `reports a synchronous failure through the listener`() {
        val failure = IllegalArgumentException("subject refused to run")
        val pluginClient = PluginClient(delegate, FailingSubject(threadPool, failure))
        val listener = RecordingListener(threadPool)

        pluginClient.execute(TEST_ACTION, StubRequest(), listener)

        assertFalse(delegate.executed)
        assertTrue(listener.invoked)
        assertEquals(failure, listener.failure)
    }

    @Test
    fun `rejects execution before a subject is assigned`() {
        val pluginClient = PluginClient(delegate)

        assertThrows(IllegalStateException::class.java) {
            pluginClient.execute(TEST_ACTION, StubRequest(), RecordingListener(threadPool))
        }
    }

    private class StubRequest : ActionRequest() {
        override fun validate(): ActionRequestValidationException? = null
    }

    private class StubResponse : ActionResponse() {
        override fun writeTo(out: StreamOutput) = Unit
    }

    /**
     * Records what the delegate saw, and keeps the listener it was given so a test can complete it
     * later, the way a transport response does.
     */
    private class RecordingClient(threadPool: ThreadPool) : AbstractClient(Settings.EMPTY, threadPool) {
        var executed = false
        var headerSeen: String? = null
        private var listener: ActionListener<*>? = null

        override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
            action: ActionType<Response>,
            request: Request,
            listener: ActionListener<Response>
        ) {
            executed = true
            headerSeen = threadPool().threadContext.getHeader(CALLER_HEADER)
            this.listener = listener
        }

        @Suppress("UNCHECKED_CAST")
        fun capturedListener(): ActionListener<StubResponse> = listener as ActionListener<StubResponse>

        override fun close() = Unit
    }

    private class RecordingListener(private val threadPool: ThreadPool) : ActionListener<StubResponse> {
        var invoked = false
        var failure: Exception? = null
        var headerOnInvocation: String? = null

        override fun onResponse(response: StubResponse) {
            invoked = true
            headerOnInvocation = threadPool.threadContext.getHeader(CALLER_HEADER)
        }

        override fun onFailure(e: Exception) {
            invoked = true
            failure = e
            headerOnInvocation = threadPool.threadContext.getHeader(CALLER_HEADER)
        }
    }

    /**
     * Mirrors the real subjects: both the security plugin's and core's noop implementation wrap the
     * body in a stashed context, so they restore the calling thread themselves.
     */
    private open class StashingSubject(private val threadPool: ThreadPool) : Subject {
        override fun getPrincipal(): Principal = NamedPrincipal("plugin_subject")

        override fun <E : Exception> runAs(runnable: CheckedRunnable<E>) {
            val stashed = threadPool.threadContext.stashContext()
            try {
                body(runnable)
            } finally {
                stashed.close()
            }
        }

        protected open fun <E : Exception> body(runnable: CheckedRunnable<E>) = runnable.run()
    }

    private class FailingSubject(threadPool: ThreadPool, private val failure: Exception) : StashingSubject(threadPool) {
        override fun <E : Exception> body(runnable: CheckedRunnable<E>): Unit = throw failure
    }
}
