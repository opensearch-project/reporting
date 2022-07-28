/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integTest.rest

import org.opensearch.integTest.PluginRestTestCase
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.integTest.jsonify
import org.opensearch.integTest.validateErrorResponse
import org.opensearch.integTest.validateTimeRecency
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.junit.Assert
import java.time.Instant

class InContextMenuReportGenerationIT : PluginRestTestCase() {
    companion object {
        private const val TEST_REPORT_DEFINITION_ID = "fake-id-123"
    }
    fun `test create in-context-menu report`() {
        val downloadRequest = """
            {
                "beginTimeMs": 1604425466263,
                "endTimeMs": 1604429066263,
                "reportDefinitionDetails": {
                    "id": "$TEST_REPORT_DEFINITION_ID",
                    "lastUpdatedTimeMs": 1604429066263,
                    "createdTimeMs": 1604429066263,
                    "reportDefinition": {
                        "name": "name",
                        "isEnabled": true,
                        "source": {
                            "description": "description",
                            "type": "Dashboard",
                            "origin":"localhost:5601",
                            "id": "id"
                        },
                        "format": {
                            "duration": "PT1H",
                            "fileFormat": "Pdf"
                        },
                        "trigger": {
                            "triggerType": "Download"
                        }
                    }
                },
                "status": "Success",
                "statusText":"statusText",
                "inContextDownloadUrlPath": "/app/dashboard#view/dashboard-id"
            }
        """.trimIndent()
        val downloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.OK.status
        )
        Assert.assertNotNull("reportInstance should be generated", downloadResponse)
        val reportInstance = downloadResponse.get("reportInstance").asJsonObject
        val reportDefinitionDetails = reportInstance.get("reportDefinitionDetails").asJsonObject
        Assert.assertEquals(
            TEST_REPORT_DEFINITION_ID,
            reportDefinitionDetails.get("id").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("beginTimeMs").asString,
            reportInstance.get("beginTimeMs").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("endTimeMs").asString,
            reportInstance.get("endTimeMs").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("status").asString,
            reportInstance.get("status").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("statusText").asString,
            reportInstance.get("statusText").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("inContextDownloadUrlPath").asString,
            reportInstance.get("inContextDownloadUrlPath").asString
        )
        validateTimeRecency(Instant.ofEpochMilli(reportInstance.get("lastUpdatedTimeMs").asLong))
        validateTimeRecency(Instant.ofEpochMilli(reportInstance.get("createdTimeMs").asLong))

        // legacy test
        val legacyDownloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.OK.status
        )
        Assert.assertNotNull("reportInstance should be generated", legacyDownloadResponse)
        val legacyReportInstance = legacyDownloadResponse.get("reportInstance").asJsonObject
        val legacyReportDefinitionDetails = legacyReportInstance.get("reportDefinitionDetails").asJsonObject
        Assert.assertEquals(
            TEST_REPORT_DEFINITION_ID,
            legacyReportDefinitionDetails.get("id").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("beginTimeMs").asString,
            legacyReportInstance.get("beginTimeMs").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("endTimeMs").asString,
            legacyReportInstance.get("endTimeMs").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("status").asString,
            legacyReportInstance.get("status").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("statusText").asString,
            legacyReportInstance.get("statusText").asString
        )
        Assert.assertEquals(
            jsonify(downloadRequest).get("inContextDownloadUrlPath").asString,
            legacyReportInstance.get("inContextDownloadUrlPath").asString
        )
        validateTimeRecency(Instant.ofEpochMilli(legacyReportInstance.get("lastUpdatedTimeMs").asLong))
        validateTimeRecency(Instant.ofEpochMilli(legacyReportInstance.get("createdTimeMs").asLong))
    }

    fun `test create in-context-menu report from invalid source request`() {
        val downloadRequest = """
            {
                "beginTimeMs": 1604425466263,
                "endTimeMs": 1604429066263,
                "reportDefinitionDetails": {
                    "id": "$TEST_REPORT_DEFINITION_ID",
                    "lastUpdatedTimeMs": 1604429066263,
                    "createdTimeMs": 1604429066263,
                    "reportDefinition": {
                        "name": "name",
                        "isEnabled": true,
                        "source": {
                            "description": "description",
                            "type": "Dashboard-invalid",
                            "origin":"localhost:5601",
                            "id": "id"
                        },
                        "format": {
                            "duration": "PT1H",
                            "fileFormat": "Pdf"
                        },
                        "trigger": {
                            "triggerType": "Download"
                        }
                    }
                },
                "status": "Success",
                "statusText":"statusText",
                "inContextDownloadUrlPath": "/app/dashboard#view/dashboard-id"
            }
        """.trimIndent()
        val downloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(downloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")

        // legacy test
        val legacyDownloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(legacyDownloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
    }

    fun `test create in-context-menu report from invalid trigger request`() {
        val downloadRequest = """
            {
                "beginTimeMs": 1604425466263,
                "endTimeMs": 1604429066263,
                "reportDefinitionDetails": {
                    "id": "$TEST_REPORT_DEFINITION_ID",
                    "lastUpdatedTimeMs": 1604429066263,
                    "createdTimeMs": 1604429066263,
                    "reportDefinition": {
                        "name": "name",
                        "isEnabled": true,
                        "source": {
                            "description": "description",
                            "type": "Dashboard",
                            "origin":"localhost:5601",
                            "id": "id"
                        },
                        "format": {
                            "duration": "PT1H",
                            "fileFormat": "Pdf"
                        },
                        "trigger": {
                            "triggerType": "CronSchedule"
                        }
                    }
                },
                "status": "Success",
                "statusText":"statusText",
                "inContextDownloadUrlPath": "/app/dashboard#view/dashboard-id"
            }
        """.trimIndent()
        val downloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(downloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")

        // legacy test
        val legacyDownloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(legacyDownloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
    }

    fun `test create in-context-menu report from invalid format request`() {
        val downloadRequest = """
            {
                "beginTimeMs": 1604425466263,
                "endTimeMs": 1604429066263,
                "reportDefinitionDetails": {
                    "id": "$TEST_REPORT_DEFINITION_ID",
                    "lastUpdatedTimeMs": 1604429066263,
                    "createdTimeMs": 1604429066263,
                    "reportDefinition": {
                        "name": "name",
                        "isEnabled": true,
                        "source": {
                            "description": "description",
                            "type": "Dashboard",
                            "origin":"localhost:5601",
                            "id": "id"
                        },
                        "format": {
                            "fileFormat": "Pdf"
                        },
                        "trigger": {
                            "triggerType": "Download"
                        }
                    }
                },
                "status": "Success",
                "statusText":"statusText",
                "inContextDownloadUrlPath": "/app/dashboard#view/dashboard-id"
            }
        """.trimIndent()
        val downloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(downloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")

        // legacy test
        val legacyDownloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(legacyDownloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
    }

    fun `test create in-context-menu report without time request`() {
        val downloadRequest = """
            {
                "reportDefinitionDetails": {
                    "id": "$TEST_REPORT_DEFINITION_ID",
                    "lastUpdatedTimeMs": 1604429066263,
                    "createdTimeMs": 1604429066263,
                    "reportDefinition": {
                        "name": "name",
                        "isEnabled": true,
                        "source": {
                            "description": "description",
                            "type": "Dashboard",
                            "origin":"localhost:5601",
                            "id": "id"
                        },
                        "format": {
                            "duration": "PT1H",
                            "fileFormat": "Pdf"
                        },
                        "trigger": {
                            "triggerType": "Download"
                        }
                    }
                },
                "status": "Success",
                "statusText":"statusText",
                "inContextDownloadUrlPath": "/app/dashboard#view/dashboard-id"
            }
        """.trimIndent()
        val downloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(downloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")

        // legacy test
        val legacyDownloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(legacyDownloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
    }

    fun `test create in-context-menu report without status request`() {
        val downloadRequest = """
            {
                "beginTimeMs": 1604425466263,
                "endTimeMs": 1604429066263,
                "reportDefinitionDetails": {
                    "id": "$TEST_REPORT_DEFINITION_ID",
                    "lastUpdatedTimeMs": 1604429066263,
                    "createdTimeMs": 1604429066263,
                    "reportDefinition": {
                        "name": "name",
                        "isEnabled": true,
                        "source": {
                            "description": "description",
                            "type": "Dashboard",
                            "origin":"localhost:5601",
                            "id": "id"
                        },
                        "format": {
                            "duration": "PT1H",
                            "fileFormat": "Pdf"
                        },
                        "trigger": {
                            "triggerType": "Download"
                        }
                    }
                },
                "statusText":"statusText",
                "inContextDownloadUrlPath": "/app/dashboard#view/dashboard-id"
            }
        """.trimIndent()
        val downloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(downloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")

        // legacy test
        val legacyDownloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(legacyDownloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
    }

    fun `test create in-context-menu report without report definition request`() {
        val downloadRequest = """
            {
                "beginTimeMs": 1604425466263,
                "endTimeMs": 1604429066263,
                "reportDefinitionDetails": {
                    "id": "$TEST_REPORT_DEFINITION_ID",
                    "lastUpdatedTimeMs": 1604429066263,
                    "createdTimeMs": 1604429066263
                },
                "status": "Success",
                "statusText":"statusText",
                "inContextDownloadUrlPath": "/app/dashboard#view/dashboard-id"
            }
        """.trimIndent()
        val downloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(downloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")

        // legacy test
        val legacyDownloadResponse = executeRequest(
            RestRequest.Method.PUT.name,
            "$LEGACY_BASE_REPORTS_URI/on_demand",
            downloadRequest,
            RestStatus.BAD_REQUEST.status
        )
        validateErrorResponse(legacyDownloadResponse, RestStatus.BAD_REQUEST.status, "illegal_argument_exception")
    }
}
