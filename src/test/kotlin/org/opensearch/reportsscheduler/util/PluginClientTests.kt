/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ActionType
import org.opensearch.common.CheckedRunnable
import org.opensearch.common.settings.Settings
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.identity.NamedPrincipal
import org.opensearch.identity.PluginSubject
import org.opensearch.threadpool.TestThreadPool
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.client.Client
import org.opensearch.transport.client.support.AbstractClient
import java.security.Principal
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

internal class PluginClientTests {

    companion object {
        private const val CALLER_TRANSIENT = "caller.marker"
        private const val SUBJECT_TRANSIENT = "subject.marker"
        private const val RESPONDER_TRANSIENT = "responder.marker"
        private val TEST_ACTION = ActionType("cluster:admin/opendistro/reports/test", Writeable.Reader { StubResponse() })
    }

    private lateinit var threadPool: ThreadPool
    private lateinit var threadContext: ThreadContext
    private lateinit var responder: ExecutorService
    private lateinit var delegate: RecordingClient
    private lateinit var pluginClient: PluginClient

    @BeforeEach
    fun setUp() {
        threadPool = TestThreadPool(PluginClientTests::class.java.name)
        threadContext = threadPool.threadContext
        responder = Executors.newSingleThreadExecutor()
        delegate = RecordingClient(threadPool)
        pluginClient = PluginClient(delegate)
    }

    @AfterEach
    fun tearDown() {
        responder.shutdownNow()
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS)
    }

    @Test
    fun `runs the action inside the subject`() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        val preassigned = PluginClient(delegate, RecordingSubject(threadContext))

        preassigned.execute(TEST_ACTION, StubRequest(), ignoringListener())

        assertEquals("set-by-subject", delegate.subjectSeen, "the action must run inside runAs")
        assertNull(delegate.callerSeen, "the caller's context must not reach the action")
    }

    @Test
    fun `listener sees the caller context and not the subject's on response`() {
        assertListenerSeesCallerContext { it.onResponse(StubResponse()) }
    }

    @Test
    fun `listener sees the caller context and not the subject's on failure`() {
        assertListenerSeesCallerContext { it.onFailure(RuntimeException("action failed")) }
    }

    @Test
    fun `restores the responding thread's context after the listener returns`() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        pluginClient.setSubject(RecordingSubject(threadContext))
        val callerSeen = AtomicReference<String?>("listener did not run")
        pluginClient.execute(
            TEST_ACTION,
            StubRequest(),
            ActionListener.wrap({ callerSeen.set(threadContext.getTransient(CALLER_TRANSIENT)) }, {})
        )

        // Restoring the caller's context and leaving it there would strand it on a pooled thread for
        // whatever runs next.
        val responderAfter = AtomicReference<String?>()
        val callerLeftBehind = AtomicReference<String?>()
        completeOnAnotherThread {
            it.onResponse(StubResponse())
            responderAfter.set(threadContext.getTransient(RESPONDER_TRANSIENT))
            callerLeftBehind.set(threadContext.getTransient(CALLER_TRANSIENT))
        }

        assertEquals("set-by-caller", callerSeen.get())
        assertEquals("set-by-responder", responderAfter.get(), "the responding thread's own context must come back")
        assertNull(callerLeftBehind.get(), "the caller's context must not be left on the responding thread")
    }

    @Test
    fun `listener sees a response header the action added`() {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        pluginClient.setSubject(RecordingSubject(threadContext))
        val callerSeen = AtomicReference<String?>("listener did not run")
        val warnings = AtomicReference<List<String>?>()
        pluginClient.execute(
            TEST_ACTION,
            StubRequest(),
            ActionListener.wrap(
                {
                    callerSeen.set(threadContext.getTransient(CALLER_TRANSIENT))
                    warnings.set(threadContext.responseHeaders["Warning"])
                },
                {}
            )
        )

        completeOnAnotherThread {
            threadContext.addResponseHeader("Warning", "deprecated")
            it.onResponse(StubResponse())
        }

        assertEquals("set-by-caller", callerSeen.get())
        assertEquals(listOf("deprecated"), warnings.get())
    }

    @Test
    fun `throws a delegate failure to the caller as the wrapped client would`() {
        pluginClient.setSubject(RecordingSubject(threadContext))
        val boom = RuntimeException("failed to find action")
        delegate.behaviour = { throw boom }

        val failures = mutableListOf<Exception>()
        val thrown = assertThrows(RuntimeException::class.java) {
            pluginClient.execute(TEST_ACTION, StubRequest(), ActionListener.wrap({}, { failures.add(it) }))
        }

        assertSame(boom, thrown)
        assertEquals(emptyList<Exception>(), failures, "a failure thrown before the listener exists must not also reach it")
    }

    @Test
    fun `does not notify again when the listener itself throws`() {
        pluginClient.setSubject(RecordingSubject(threadContext))
        delegate.behaviour = { it.onResponse(StubResponse()) }

        val listenerBug = RuntimeException("listener bug")
        val calls = AtomicInteger()
        val throwing = object : ActionListener<StubResponse> {
            override fun onResponse(response: StubResponse) {
                calls.incrementAndGet()
                throw listenerBug
            }

            override fun onFailure(e: Exception) {
                calls.incrementAndGet()
            }
        }

        val surfaced = assertThrows(RuntimeException::class.java) {
            pluginClient.execute(TEST_ACTION, StubRequest(), throwing)
        }
        assertSame(listenerBug, surfaced)
        assertEquals(1, calls.get(), "the listener must be told exactly once")
    }

    @Test
    fun `rejects use before a subject is assigned`() {
        val e = assertThrows(IllegalStateException::class.java) {
            pluginClient.execute(TEST_ACTION, StubRequest(), ignoringListener())
        }
        assertTrue(e.message.orEmpty().contains("subject"))
        assertFalse(delegate.executed, "the action must not run without a subject")
    }

    @Test
    fun `accepts the same subject twice but rejects a different one`() {
        val first = RecordingSubject(threadContext, "first")
        pluginClient.setSubject(first)
        pluginClient.setSubject(first)

        assertThrows(IllegalStateException::class.java) { pluginClient.setSubject(RecordingSubject(threadContext, "second")) }

        pluginClient.execute(TEST_ACTION, StubRequest(), ignoringListener())
        assertEquals("first", delegate.subjectSeen, "a rejected subject must not replace the assigned one")
    }

    @Test
    fun `refuses a remote cluster client`() {
        assertThrows(UnsupportedOperationException::class.java) { pluginClient.getRemoteClusterClient("remote") }
    }

    private fun assertListenerSeesCallerContext(completion: (ActionListener<StubResponse>) -> Unit) {
        threadContext.putTransient(CALLER_TRANSIENT, "set-by-caller")
        pluginClient.setSubject(RecordingSubject(threadContext))

        val callerSeen = AtomicReference<String?>("listener did not run")
        val subjectSeen = AtomicReference<String?>("listener did not run")
        val record = { _: Any ->
            callerSeen.set(threadContext.getTransient(CALLER_TRANSIENT))
            subjectSeen.set(threadContext.getTransient(SUBJECT_TRANSIENT))
        }
        pluginClient.execute(TEST_ACTION, StubRequest(), ActionListener.wrap(record, record))

        // A response arrives on a pooled thread that never carried the caller's context, so the wrapper
        // around the listener is the only thing that can put it back.
        completeOnAnotherThread(completion)

        assertEquals("set-by-caller", callerSeen.get())
        assertNull(subjectSeen.get(), "the subject's context must not leak into the listener")
    }

    private fun ignoringListener(): ActionListener<StubResponse> = ActionListener.wrap({}, {})

    /** Completes the captured action on a thread that never carried the caller's context. */
    private fun completeOnAnotherThread(completion: (ActionListener<StubResponse>) -> Unit) {
        responder.submit {
            threadContext.putTransient(RESPONDER_TRANSIENT, "set-by-responder")
            completion(delegate.capturedListener())
        }.get(10, TimeUnit.SECONDS)
    }

    private class StubRequest : ActionRequest() {
        override fun validate(): ActionRequestValidationException? = null
    }

    private class StubResponse : ActionResponse() {
        override fun writeTo(out: StreamOutput) = Unit
    }

    /**
     * Records the context the action ran in and keeps its listener, so each test decides when and how
     * the action completes. By default it does not complete, as for an asynchronous action.
     */
    private class RecordingClient(threadPool: ThreadPool) : AbstractClient(Settings.EMPTY, threadPool) {
        var executed = false
        var subjectSeen: String? = null
        var callerSeen: String? = null
        var behaviour: (ActionListener<StubResponse>) -> Unit = {}
        private var listener: ActionListener<StubResponse>? = null

        @Suppress("UNCHECKED_CAST")
        override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
            action: ActionType<Response>,
            request: Request,
            listener: ActionListener<Response>
        ) {
            executed = true
            subjectSeen = threadPool().threadContext.getTransient(SUBJECT_TRANSIENT)
            callerSeen = threadPool().threadContext.getTransient(CALLER_TRANSIENT)
            val captured = listener as ActionListener<StubResponse>
            this.listener = captured
            behaviour(captured)
        }

        fun capturedListener(): ActionListener<StubResponse> = checkNotNull(listener) { "the action was not executed" }

        // Hands back a client that does not run as the subject, as a node client does.
        override fun getRemoteClusterClient(clusterAlias: String): Client = this

        override fun close() = Unit
    }

    /**
     * Stands in for the plugin subject. Stashes and restores around its body as NoopPluginSubject and
     * SecurePluginSubject do, and marks the context so a test can tell the subject's apart from the
     * caller's.
     */
    private class RecordingSubject(
        private val threadContext: ThreadContext,
        private val marker: String = "set-by-subject"
    ) : PluginSubject {
        override fun getPrincipal(): Principal = NamedPrincipal("plugin:test")

        override fun <E : Exception> runAs(runnable: CheckedRunnable<E>) {
            threadContext.stashContext().use {
                threadContext.putTransient(SUBJECT_TRANSIENT, marker)
                runnable.run()
            }
        }
    }
}
