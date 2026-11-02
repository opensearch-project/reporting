/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.security

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.model.ReportActionType

internal class UserAccessManagerTests {

    private fun user(tenant: String?, tenantAccess: String?, roles: List<String> = listOf()): User {
        return User("test_user", listOf("backend_role"), roles, mapOf(), tenant, tenantAccess)
    }

    @Test
    fun `create is allowed when security is disabled`() {
        assertTrue(UserAccessManager.doesUserHasAccessToCreate(null))
    }

    @Test
    fun `create in the private tenant does not depend on tenant access`() {
        assertTrue(UserAccessManager.doesUserHasAccessToCreate(user("__user__", "NONE")))
        assertTrue(UserAccessManager.doesUserHasAccessToCreate(user("__user__", null)))
    }

    @Test
    fun `create in a shared tenant requires write tenant access`() {
        assertTrue(UserAccessManager.doesUserHasAccessToCreate(user("team", "WRITE")))
        assertFalse(UserAccessManager.doesUserHasAccessToCreate(user("team", "READ")))
        assertFalse(UserAccessManager.doesUserHasAccessToCreate(user("team", "NONE")))
        assertFalse(UserAccessManager.doesUserHasAccessToCreate(user("", "READ")))
    }

    @Test
    fun `create is allowed for all_access users regardless of tenant access`() {
        assertTrue(UserAccessManager.doesUserHasAccessToCreate(user("team", "READ", listOf("all_access"))))
    }

    @Test
    fun `missing tenant access keeps previous behavior`() {
        assertTrue(UserAccessManager.doesUserHasAccessToCreate(user("team", null)))
    }

    @Test
    fun `private tenant access only depends on ownership`() {
        val privateUser = user("__user__", "NONE")
        val ownAccess = listOf("User:test_user")
        assertTrue(UserAccessManager.doesUserHasAccess(privateUser, "__user__", ownAccess, ReportActionType.UPDATE))
        assertFalse(UserAccessManager.doesUserHasAccess(privateUser, "__user__", listOf("User:other"), ReportActionType.READ))
    }

    @Test
    fun `shared tenant access is denied without the required tenant access`() {
        val access = listOf("BERole:backend_role")
        assertFalse(UserAccessManager.doesUserHasAccess(user("team", "READ"), "team", access, ReportActionType.UPDATE))
        assertFalse(UserAccessManager.doesUserHasAccess(user("team", "READ"), "team", access, ReportActionType.DELETE))
        assertFalse(UserAccessManager.doesUserHasAccess(user("team", "NONE"), "team", access, ReportActionType.READ))
    }

    @Test
    fun `access to another tenant is denied`() {
        assertFalse(UserAccessManager.doesUserHasAccess(user("team", "WRITE"), "other", listOf(), ReportActionType.READ))
    }
}
