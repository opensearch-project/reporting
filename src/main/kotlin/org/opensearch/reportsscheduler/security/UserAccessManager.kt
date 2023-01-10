/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.security

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.settings.PluginSettings
import org.opensearch.rest.RestStatus

/**
 * Class for checking/filtering user access.
 */
internal object UserAccessManager {
    private const val USER_TAG = "User:"
    private const val ROLE_TAG = "Role:"
    private const val BACKEND_ROLE_TAG = "BERole:"
    private const val PRIVATE_TENANT = "__user__"
    const val DEFAULT_TENANT = ""

    /**
     * Validate User if eligible to do operation
     * If filterBy == NoFilter
     *  -> No validation
     * If filterBy == User
     *  -> User name should be present
     * If filterBy == Roles
     *  -> roles should be present
     * If filterBy == BackendRoles
     *  -> backend roles should be present
     */
    fun validateUser(user: User?) {
        if (isUserPrivateTenant(user) && user?.name == null) {
            Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException(
                "User name not provided for private tenant access",
                RestStatus.FORBIDDEN
            )
        }
        if (PluginSettings.isRbacEnabled()) {
            // backend roles must be present
            if (user?.backendRoles.isNullOrEmpty()) {
                Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
                throw OpenSearchStatusException(
                    "User doesn't have backend roles configured. Contact administrator.",
                    RestStatus.FORBIDDEN
                )
            }
        }
    }

    /**
     * Get tenant info from user object.
     */
    fun getUserTenant(user: User?): String {
        return when (val requestedTenant = user?.requestedTenant) {
            null -> DEFAULT_TENANT
            else -> requestedTenant
        }
    }

    /**
     * Get all user access info from user object.
     */
    fun getAllAccessInfo(user: User?): List<String> {
        if (user == null) { // Security is disabled
            return listOf()
        }
        val retList: MutableList<String> = mutableListOf()
        if (user.name != null) {
            retList.add("$USER_TAG${user.name}")
        }
        user.roles.forEach { retList.add("$ROLE_TAG$it") }
        user.backendRoles.forEach { retList.add("$BACKEND_ROLE_TAG$it") }
        return retList
    }

    /**
     * Get user object from all user access info.
     */
    fun getUserFromAccess(access: List<String>): User? {
        if (access.isNullOrEmpty()) {
            return null
        }
        val name = access.find { it.startsWith(USER_TAG) }?.substring(USER_TAG.length)
        val backendRoles = access.filter { it.startsWith(ROLE_TAG) }.map { it.substring(ROLE_TAG.length) }
        val roles = access.filter { it.startsWith(BACKEND_ROLE_TAG) }.map { it.substring(BACKEND_ROLE_TAG.length) }
        return User(name, backendRoles, roles, listOf())
    }

    /**
     * Get access info for search filtering
     */
    fun getSearchAccessInfo(user: User?): List<String> {
        if (user == null) { // Security is disabled
            return listOf()
        }
        if (isUserPrivateTenant(user)) {
            return listOf("$USER_TAG${user.name}") // No sharing allowed in private tenant.
        }
        return if (PluginSettings.isRbacEnabled()) {
            user.backendRoles.map { "$BACKEND_ROLE_TAG$it" }
        } else {
            listOf()
        }
    }

    /**
     * validate if user has access based on given access list
     */
    fun doesUserHasAccess(user: User?, tenant: String, access: List<String>): Boolean {
        if (user == null) { // Security is disabled
            return true
        }
        if (getUserTenant(user) != tenant) {
            return false
        }
        return if (PluginSettings.isRbacEnabled()) {
            user.backendRoles.map { "$BACKEND_ROLE_TAG$it" }.any { it in access }
        } else {
            true
        }
    }

    private fun isUserPrivateTenant(user: User?): Boolean {
        return getUserTenant(user) == PRIVATE_TENANT
    }
}
