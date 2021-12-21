/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integTest.bwc

import org.junit.Assert
import org.opensearch.common.settings.Settings
import org.opensearch.integTest.PluginRestTestCase
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.integTest.constructReportDefinitionRequest
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import java.time.Instant

class ReportsSchedulerBackwardsCompatibilityIT : PluginRestTestCase() {

    companion object {
        private val CLUSTER_TYPE = ClusterType.parse(System.getProperty("tests.rest.bwcsuite"))
        private val CLUSTER_NAME = System.getProperty("tests.clustername")
    }

    override fun preserveIndicesUponCompletion(): Boolean = true

    override fun preserveReposUponCompletion(): Boolean = true

    override fun preserveTemplatesUponCompletion(): Boolean = true

    override fun preserveODFEIndicesAfterTest(): Boolean = true

    override fun restClientSettings(): Settings {
        return Settings.builder()
            .put(super.restClientSettings())
            // increase the timeout here to 90 seconds to handle long waits for a green
            // cluster health. the waits for green need to be longer than a minute to
            // account for delayed shards
            .put(CLIENT_SOCKET_TIMEOUT, "90s")
            .build()
    }

    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    fun `test backwards compatibility`() {
        val uri = getPluginUri()
        val responseMap = getAsMap(uri)["nodes"] as Map<String, Map<String, Any>>
        for (response in responseMap.values) {
            val plugins = response["plugins"] as List<Map<String, Any>>
            val pluginNames = plugins.map { plugin -> plugin["name"] }.toSet()
            when (CLUSTER_TYPE) {
                ClusterType.OLD -> {
                    assertTrue(pluginNames.contains("opendistro-reports-scheduler"))
                    assertTrue(pluginNames.contains("opendistro-job-scheduler"))
                    createBasicReportDefinition()
                }
                ClusterType.MIXED -> {
                    assertTrue(pluginNames.contains("opensearch-reports-scheduler"))
                    assertTrue(pluginNames.contains("opensearch-job-scheduler"))
                    verifyReportDefinitionExists(LEGACY_BASE_REPORTS_URI)
                    verifyReportInstanceExists(LEGACY_BASE_REPORTS_URI)
                }
                ClusterType.UPGRADED -> {
                    assertTrue(pluginNames.contains("opensearch-reports-scheduler"))
                    assertTrue(pluginNames.contains("opensearch-job-scheduler"))
                    verifyReportDefinitionExists(BASE_REPORTS_URI)
                    verifyReportInstanceExists(BASE_REPORTS_URI)
                }
            }
            break
        }
    }

    private enum class ClusterType {
        OLD,
        MIXED,
        UPGRADED;

        companion object {
            fun parse(value: String): ClusterType {
                return when (value) {
                    "old_cluster" -> OLD
                    "mixed_cluster" -> MIXED
                    "upgraded_cluster" -> UPGRADED
                    else -> throw AssertionError("Unknown cluster type: $value")
                }
            }
        }
    }

    private fun getPluginUri(): String {
        return when (CLUSTER_TYPE) {
            ClusterType.OLD -> "_nodes/$CLUSTER_NAME-0/plugins"
            ClusterType.MIXED -> {
                when (System.getProperty("tests.rest.bwcsuite_round")) {
                    "second" -> "_nodes/$CLUSTER_NAME-1/plugins"
                    "third" -> "_nodes/$CLUSTER_NAME-2/plugins"
                    else -> "_nodes/$CLUSTER_NAME-0/plugins"
                }
            }
            ClusterType.UPGRADED -> "_nodes/plugins"
        }
    }

    @Throws(Exception::class)
    private fun createBasicReportDefinition() {
        val timeStampMillis = Instant.now().toEpochMilli()
        val trigger = """
            "trigger":{
                "triggerType":"IntervalSchedule",
                "schedule":{
                    "interval":{
                        "start_time":$timeStampMillis,
                        "period":"1",
                        "unit":"Minutes"
                    }
                }
            },
        """.trimIndent()
        val reportDefinitionRequest = constructReportDefinitionRequest(trigger)

        // legacy test
        val legacyReportDefinitionResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$LEGACY_BASE_REPORTS_URI/definition",
            reportDefinitionRequest,
            RestStatus.OK.status
        )
        val legacyReportDefinitionId = legacyReportDefinitionResponse.get("reportDefinitionId").asString
        Assert.assertNotNull("reportDefinitionId should be generated", legacyReportDefinitionId)
        // adding this wait time for the scheduler to create a report instance in the report-instance index,
        // based on this report definition
        Thread.sleep(610000)
    }

    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    private fun verifyReportDefinitionExists(uri: String) {
        val listReportDefinitions = executeRequest(
            RestRequest.Method.GET.name,
            "$uri/definitions",
            "",
            RestStatus.OK.status
        )
        val totalHits = listReportDefinitions.get("totalHits").asInt
        Assert.assertEquals(totalHits, 1)
    }

    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    private fun verifyReportInstanceExists(uri: String) {
        val listReportInstances = executeRequest(
            RestRequest.Method.GET.name,
            "$uri/instances",
            "",
            RestStatus.OK.status
        )
        val totalHits = listReportInstances.get("totalHits").asInt
        assertTrue("Actual report instances counts ($totalHits) should be greater than or equal to (1)", totalHits >= 1)
    }
}
