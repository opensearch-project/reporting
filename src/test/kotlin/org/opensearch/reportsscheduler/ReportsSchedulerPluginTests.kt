/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objenesis.ObjenesisStd
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ActionType
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.CheckedRunnable
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.Settings
import org.opensearch.common.util.concurrent.ThreadContext
import org.opensearch.core.action.ActionListener
import org.opensearch.core.action.ActionResponse
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.env.NodeEnvironment
import org.opensearch.identity.NamedPrincipal
import org.opensearch.identity.PluginSubject
import org.opensearch.reportsscheduler.settings.PluginSettings
import org.opensearch.reportsscheduler.util.PluginClient
import org.opensearch.threadpool.TestThreadPool
import org.opensearch.threadpool.ThreadPool
import org.opensearch.transport.client.Client
import org.opensearch.transport.client.support.AbstractClient
import java.security.Principal
import java.util.concurrent.TimeUnit

internal class ReportsSchedulerPluginTests {

    companion object {
        private const val SUBJECT_TRANSIENT = "subject.marker"
        private val TEST_ACTION = ActionType("cluster:admin/opendistro/reports/test", Writeable.Reader { StubResponse() })
    }

    private lateinit var threadPool: ThreadPool

    @BeforeEach
    fun setUp() {
        threadPool = TestThreadPool(ReportsSchedulerPluginTests::class.java.name)
    }

    @AfterEach
    fun tearDown() {
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS)
    }

    @Test
    fun `assignSubject gives the plugin's client the subject its actions run as`() {
        val plugin = ReportsSchedulerPlugin()
        val delegate = RecordingClient(threadPool)
        val pluginClient = createComponents(plugin, delegate).filterIsInstance<PluginClient>().single()

        plugin.assignSubject(MarkingSubject(threadPool.threadContext, "assigned-subject"))
        pluginClient.execute(TEST_ACTION, StubRequest(), ActionListener.wrap({}, {}))

        assertEquals("assigned-subject", delegate.subjectSeen)
    }

    private fun createComponents(plugin: ReportsSchedulerPlugin, client: Client): Collection<Any> {
        val clusterService = mock<ClusterService>()
        whenever(clusterService.settings).thenReturn(Settings.EMPTY)
        whenever(clusterService.clusterSettings)
            .thenReturn(ClusterSettings(Settings.EMPTY, PluginSettings.getAllSettings().toSet()))
        return plugin.createComponents(
            client,
            clusterService,
            threadPool,
            mock(),
            mock(),
            mock(),
            mock(),
            // NodeEnvironment is final, Mockito here makes subclass mocks only, and createComponents does not use it.
            ObjenesisStd().newInstance(NodeEnvironment::class.java),
            mock(),
            mock(),
            mock()
        )
    }

    private class StubRequest : ActionRequest() {
        override fun validate(): ActionRequestValidationException? = null
    }

    private class StubResponse : ActionResponse() {
        override fun writeTo(out: StreamOutput) = Unit
    }

    /** Records which subject, if any, the action ran as. */
    private class RecordingClient(threadPool: ThreadPool) : AbstractClient(Settings.EMPTY, threadPool) {
        var subjectSeen: String? = null

        override fun <Request : ActionRequest, Response : ActionResponse> doExecute(
            action: ActionType<Response>,
            request: Request,
            listener: ActionListener<Response>
        ) {
            subjectSeen = threadPool().threadContext.getTransient(SUBJECT_TRANSIENT)
        }

        override fun close() = Unit
    }

    /** Stashes and restores around its body, as the real plugin subjects do, and marks the context. */
    private class MarkingSubject(private val threadContext: ThreadContext, private val marker: String) : PluginSubject {
        override fun getPrincipal(): Principal = NamedPrincipal("plugin:test")

        override fun <E : Exception> runAs(runnable: CheckedRunnable<E>) {
            threadContext.stashContext().use {
                threadContext.putTransient(SUBJECT_TRANSIENT, marker)
                runnable.run()
            }
        }
    }
}
