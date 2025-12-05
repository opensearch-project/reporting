/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integTest.rest

import org.opensearch.core.rest.RestStatus
import org.opensearch.integTest.PluginRestTestCase
import org.opensearch.integTest.constructReportDefinitionRequest
import org.opensearch.integTest.validateErrorResponse
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LEGACY_BASE_REPORTS_URI
import org.opensearch.reportsscheduler.resources.Utils
import org.opensearch.rest.RestRequest

/**
 * Integration tests for resource sharing feature.
 * * Tests the behavior difference between backend_roles filtering and resource-sharing:
 * - Without resource-sharing: Users with same backend_role have default access
 * - With resource-sharing: Users need explicit access grants, regardless of roles
 */
class ResourceSharingIT : PluginRestTestCase() {

    /**
     * Test report definition CRUD operations with resource sharing enabled.
     * Verifies that full access user cannot access resources until explicitly shared.
     */
    @Suppress("LongMethod")
    private fun testReportDefinitionCRUDWithResourceSharing(baseUri: String) {
        if (!isResourceSharingFeatureEnabled()) return

        val reportDefinitionId = createReportDefinitionAndVerifyOwnerAccess(baseUri)
        verifyNoAccessWithoutSharing(baseUri, reportDefinitionId)
        testReadOnlyAccessSharing(baseUri, reportDefinitionId)
        testFullAccessSharing(baseUri, reportDefinitionId)
        cleanupReportDefinition(baseUri, reportDefinitionId)
    }

    private fun createReportDefinitionAndVerifyOwnerAccess(baseUri: String): String {
        val createRequest = constructReportDefinitionRequest()
        val createResponse = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/definition",
            createRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = createResponse.get("reportDefinitionId").asString
        assertNotNull("reportDefinitionId should be generated", reportDefinitionId)
        Thread.sleep(100)

        val ownerGet = executeRequest(
            reportsFullClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
        assertEquals(
            reportDefinitionId,
            ownerGet.get("reportDefinitionDetails").asJsonObject.get("id").asString
        )
        return reportDefinitionId
    }

    private fun verifyNoAccessWithoutSharing(baseUri: String, reportDefinitionId: String) {
        val readGetBefore = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.FORBIDDEN.status
        )
        validateErrorResponse(readGetBefore, RestStatus.FORBIDDEN.status)

        val noAccessGetBefore = executeRequest(
            reportsNoAccessClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.FORBIDDEN.status
        )
        validateErrorResponse(noAccessGetBefore, RestStatus.FORBIDDEN.status)
    }

    private fun testReadOnlyAccessSharing(baseUri: String, reportDefinitionId: String) {
        val shareReadPayload = shareWithUserPayload(
            reportDefinitionId,
            Utils.REPORT_DEFINITION_TYPE,
            reportReadOnlyAccessLevel,
            reportReadUser
        )
        shareConfig(reportsFullClient, shareReadPayload)

        waitForSharingVisibility(
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            null,
            reportsReadClient
        )

        val readGetAfter = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
        assertEquals(
            reportDefinitionId,
            readGetAfter.get("reportDefinitionDetails").asJsonObject.get("id").asString
        )

        val updateRequest = constructReportDefinitionRequest(name = "read_user_update_attempt")
        val readUpdateForbidden = executeRequest(
            reportsReadClient,
            RestRequest.Method.PUT.name,
            "$baseUri/definition/$reportDefinitionId",
            updateRequest,
            RestStatus.FORBIDDEN.status
        )
        validateErrorResponse(readUpdateForbidden, RestStatus.FORBIDDEN.status)

        val readDeleteForbidden = executeRequest(
            reportsReadClient,
            RestRequest.Method.DELETE.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.FORBIDDEN.status
        )
        validateErrorResponse(readDeleteForbidden, RestStatus.FORBIDDEN.status)
    }

    private fun testFullAccessSharing(baseUri: String, reportDefinitionId: String) {
        val shareNoAccessPayload = shareWithUserPayload(
            reportDefinitionId,
            Utils.REPORT_DEFINITION_TYPE,
            reportFullAccessLevel,
            reportNoAccessUser
        )
        shareConfig(reportsFullClient, shareNoAccessPayload)

        waitForSharingVisibility(
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            null,
            reportsNoAccessClient
        )

        val noAccessGetAfter = executeRequest(
            reportsNoAccessClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
        assertEquals(
            reportDefinitionId,
            noAccessGetAfter.get("reportDefinitionDetails").asJsonObject.get("id").asString
        )

        val noAccessUpdateRequest = constructReportDefinitionRequest(name = "no_access_user_updated")
        val noAccessUpdate = executeRequest(
            reportsNoAccessClient,
            RestRequest.Method.PUT.name,
            "$baseUri/definition/$reportDefinitionId",
            noAccessUpdateRequest,
            RestStatus.OK.status
        )
        assertEquals(
            reportDefinitionId,
            noAccessUpdate.get("reportDefinitionId").asString
        )

        val verifyUpdate = executeRequest(
            reportsFullClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
        assertEquals(
            "no_access_user_updated",
            verifyUpdate.get("reportDefinitionDetails").asJsonObject
                .get("reportDefinition").asJsonObject.get("name").asString
        )
    }

    private fun cleanupReportDefinition(baseUri: String, reportDefinitionId: String) {
        val deleteResponse = executeRequest(
            reportsFullClient,
            RestRequest.Method.DELETE.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
        assertEquals(
            reportDefinitionId,
            deleteResponse.get("reportDefinitionId").asString
        )
    }

    fun `test report definition CRUD with resource sharing`() {
        testReportDefinitionCRUDWithResourceSharing(BASE_REPORTS_URI)
    }

    fun `test legacy report definition CRUD with resource sharing`() {
        testReportDefinitionCRUDWithResourceSharing(LEGACY_BASE_REPORTS_URI)
    }

    /**
     * Test listing report definitions with resource sharing.
     * Verifies that users only see resources they own or have been granted access to.
     */
    private fun testListReportDefinitionsWithResourceSharing(baseUri: String) {
        if (!isResourceSharingFeatureEnabled()) return

        // Create multiple report definitions with full access user
        val def1Request = constructReportDefinitionRequest(name = "definition_1")
        val def1Response = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/definition",
            def1Request,
            RestStatus.OK.status
        )
        val def1Id = def1Response.get("reportDefinitionId").asString

        val def2Request = constructReportDefinitionRequest(name = "definition_2")
        val def2Response = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/definition",
            def2Request,
            RestStatus.OK.status
        )
        val def2Id = def2Response.get("reportDefinitionId").asString

        val def3Request = constructReportDefinitionRequest(name = "definition_3")
        val def3Response = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/definition",
            def3Request,
            RestStatus.OK.status
        )
        val def3Id = def3Response.get("reportDefinitionId").asString
        Thread.sleep(1000)

        // Owner sees all their definitions
        val ownerList = executeRequest(
            reportsFullClient,
            RestRequest.Method.GET.name,
            "$baseUri/definitions",
            "",
            RestStatus.OK.status
        )
        assertEquals(3, ownerList.get("totalHits").asInt)

        // Read user sees none (no sharing yet)
        val readListBefore = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/definitions",
            "",
            RestStatus.OK.status
        )
        assertEquals(0, readListBefore.get("totalHits").asInt)

        // No-access user sees none
        val noAccessListBefore = executeRequest(
            reportsNoAccessClient,
            RestRequest.Method.GET.name,
            "$baseUri/definitions",
            "",
            RestStatus.OK.status
        )
        assertEquals(0, noAccessListBefore.get("totalHits").asInt)

        // Share def1 with read user
        val shareDef1 = shareWithUserPayload(
            def1Id,
            Utils.REPORT_DEFINITION_TYPE,
            reportReadOnlyAccessLevel,
            reportReadUser
        )
        shareConfig(reportsFullClient, shareDef1)

        // Share def2 with no-access user
        val shareDef2 = shareWithUserPayload(
            def2Id,
            Utils.REPORT_DEFINITION_TYPE,
            reportFullAccessLevel,
            reportNoAccessUser
        )
        shareConfig(reportsFullClient, shareDef2)

        Thread.sleep(1000)

        // Read user sees only def1
        val readListAfter = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/definitions",
            "",
            RestStatus.OK.status
        )
        assertEquals(1, readListAfter.get("totalHits").asInt)
        val readList = readListAfter.get("reportDefinitionDetailsList").asJsonArray
        assertEquals(def1Id, readList[0].asJsonObject.get("id").asString)

        // No-access user sees only def2
        val noAccessListAfter = executeRequest(
            reportsNoAccessClient,
            RestRequest.Method.GET.name,
            "$baseUri/definitions",
            "",
            RestStatus.OK.status
        )
        assertEquals(1, noAccessListAfter.get("totalHits").asInt)
        val noAccessList = noAccessListAfter.get("reportDefinitionDetailsList").asJsonArray
        assertEquals(def2Id, noAccessList[0].asJsonObject.get("id").asString)

        // Cleanup
        executeRequest(reportsFullClient, RestRequest.Method.DELETE.name, "$baseUri/definition/$def1Id", "", RestStatus.OK.status)
        executeRequest(reportsFullClient, RestRequest.Method.DELETE.name, "$baseUri/definition/$def2Id", "", RestStatus.OK.status)
        executeRequest(reportsFullClient, RestRequest.Method.DELETE.name, "$baseUri/definition/$def3Id", "", RestStatus.OK.status)
    }

    fun `test list report definitions with resource sharing`() {
        testListReportDefinitionsWithResourceSharing(BASE_REPORTS_URI)
    }

    fun `test legacy list report definitions with resource sharing`() {
        testListReportDefinitionsWithResourceSharing(LEGACY_BASE_REPORTS_URI)
    }

    /**
     * Test patch operations for sharing (add/revoke access).
     */
    private fun testPatchSharingOperations(baseUri: String) {
        if (!isResourceSharingFeatureEnabled()) return

        val createRequest = constructReportDefinitionRequest(name = "patch_test")
        val createResponse = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/definition",
            createRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = createResponse.get("reportDefinitionId").asString
        Thread.sleep(100)

        // Share with read user using patch
        val patchSharePayload = PatchSharingInfoPayloadBuilder()
            .configId(reportDefinitionId)
            .configType(Utils.REPORT_DEFINITION_TYPE)
            .apply {
                share(
                    mutableMapOf(Recipient.USERS to mutableSetOf(reportReadUser)),
                    reportReadOnlyAccessLevel
                )
            }
            .build()

        patchSharingInfo(reportsFullClient, patchSharePayload)

        // Wait for sharing to propagate
        waitForSharingVisibility(
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            null,
            reportsReadClient
        )

        // Read user can access
        val readGet = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
        assertNotNull(readGet)

        // Revoke access using patch
        val patchRevokePayload = PatchSharingInfoPayloadBuilder()
            .configId(reportDefinitionId)
            .configType(Utils.REPORT_DEFINITION_TYPE)
            .apply {
                revoke(
                    mutableMapOf(Recipient.USERS to mutableSetOf(reportReadUser)),
                    reportReadOnlyAccessLevel
                )
            }
            .build()

        patchSharingInfo(reportsFullClient, patchRevokePayload)

        // Wait for revocation to propagate
        waitForRevokeNonVisibility(
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            null,
            reportsReadClient
        )

        // Read user cannot access anymore
        val readGetAfterRevoke = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.FORBIDDEN.status
        )
        validateErrorResponse(readGetAfterRevoke, RestStatus.FORBIDDEN.status)

        // Cleanup
        executeRequest(
            reportsFullClient,
            RestRequest.Method.DELETE.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
    }

    fun `test patch sharing operations`() {
        testPatchSharingOperations(BASE_REPORTS_URI)
    }

    fun `test legacy patch sharing operations`() {
        testPatchSharingOperations(LEGACY_BASE_REPORTS_URI)
    }

    /**
     * Test report instance operations with resource sharing.
     */
    private fun testReportInstanceWithResourceSharing(baseUri: String) {
        if (!isResourceSharingFeatureEnabled()) return

        // Create report definition and generate instance
        val defRequest = constructReportDefinitionRequest()
        val defResponse = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/definition",
            defRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = defResponse.get("reportDefinitionId").asString
        Thread.sleep(100)

        val onDemandRequest = "{}"
        val onDemandResponse = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/on_demand/$reportDefinitionId",
            onDemandRequest,
            RestStatus.OK.status
        )
        val reportInstance = onDemandResponse.get("reportInstance").asJsonObject
        val reportInstanceId = reportInstance.get("id").asString
        Thread.sleep(100)

        // Owner can access instance
        val ownerGetInstance = executeRequest(
            reportsFullClient,
            RestRequest.Method.GET.name,
            "$baseUri/instance/$reportInstanceId",
            "",
            RestStatus.OK.status
        )
        assertNotNull(ownerGetInstance)

        // Read user cannot access instance
        val readGetInstanceBefore = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/instance/$reportInstanceId",
            "",
            RestStatus.FORBIDDEN.status
        )
        validateErrorResponse(readGetInstanceBefore, RestStatus.FORBIDDEN.status)

        // Share instance with read user
        val shareInstancePayload = shareWithUserPayload(
            reportInstanceId,
            Utils.REPORT_INSTANCE_TYPE,
            reportInstanceReadOnlyAccessLevel,
            reportReadUser
        )
        shareConfig(reportsFullClient, shareInstancePayload)

        // Wait for sharing to propagate
        waitForSharingVisibility(
            RestRequest.Method.GET.name,
            "$baseUri/instance/$reportInstanceId",
            null,
            reportsReadClient
        )

        // Read user can now access instance
        val readGetInstanceAfter = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/instance/$reportInstanceId",
            "",
            RestStatus.OK.status
        )
        assertEquals(
            reportInstanceId,
            readGetInstanceAfter.get("reportInstance").asJsonObject.get("id").asString
        )

        // Cleanup
        executeRequest(
            reportsFullClient,
            RestRequest.Method.DELETE.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
    }

    fun `test report instance with resource sharing`() {
        testReportInstanceWithResourceSharing(BASE_REPORTS_URI)
    }

    fun `test legacy report instance with resource sharing`() {
        testReportInstanceWithResourceSharing(LEGACY_BASE_REPORTS_URI)
    }

    /**
     * Test listing report instances with resource sharing.
     */
    private fun testListReportInstancesWithResourceSharing(baseUri: String) {
        if (!isResourceSharingFeatureEnabled()) return

        // Create report definition and generate instances
        val defRequest = constructReportDefinitionRequest()
        val defResponse = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/definition",
            defRequest,
            RestStatus.OK.status
        )
        val reportDefinitionId = defResponse.get("reportDefinitionId").asString
        Thread.sleep(100)

        val onDemandRequest = "{}"
        val instance1Response = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/on_demand/$reportDefinitionId",
            onDemandRequest,
            RestStatus.OK.status
        )
        val instance1Id = instance1Response.get("reportInstance").asJsonObject.get("id").asString

        val instance2Response = executeRequest(
            reportsFullClient,
            RestRequest.Method.POST.name,
            "$baseUri/on_demand/$reportDefinitionId",
            onDemandRequest,
            RestStatus.OK.status
        )
        instance2Response.get("reportInstance").asJsonObject.get("id").asString
        Thread.sleep(1000)

        // Owner sees all instances
        val ownerList = executeRequest(
            reportsFullClient,
            RestRequest.Method.GET.name,
            "$baseUri/instances",
            "",
            RestStatus.OK.status
        )
        assertTrue(ownerList.get("totalHits").asInt >= 2)

        // Read user sees none
        val readListBefore = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/instances",
            "",
            RestStatus.OK.status
        )
        assertEquals(0, readListBefore.get("totalHits").asInt)

        // Share instance1 with read user
        val sharePayload = shareWithUserPayload(
            instance1Id,
            Utils.REPORT_INSTANCE_TYPE,
            reportInstanceReadOnlyAccessLevel,
            reportReadUser
        )
        shareConfig(reportsFullClient, sharePayload)
        Thread.sleep(1000)

        // Read user sees only instance1
        val readListAfter = executeRequest(
            reportsReadClient,
            RestRequest.Method.GET.name,
            "$baseUri/instances",
            "",
            RestStatus.OK.status
        )
        assertEquals(1, readListAfter.get("totalHits").asInt)
        val instanceList = readListAfter.get("reportInstanceList").asJsonArray
        assertEquals(instance1Id, instanceList[0].asJsonObject.get("id").asString)

        // Cleanup
        executeRequest(
            reportsFullClient,
            RestRequest.Method.DELETE.name,
            "$baseUri/definition/$reportDefinitionId",
            "",
            RestStatus.OK.status
        )
    }

    fun `test list report instances with resource sharing`() {
        testListReportInstancesWithResourceSharing(BASE_REPORTS_URI)
    }

    fun `test legacy list report instances with resource sharing`() {
        testListReportInstancesWithResourceSharing(LEGACY_BASE_REPORTS_URI)
    }
}
