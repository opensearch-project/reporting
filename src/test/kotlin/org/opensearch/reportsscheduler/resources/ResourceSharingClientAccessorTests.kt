/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.resources

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.opensearch.reportsscheduler.ReportsSchedulerExtension
import org.opensearch.security.spi.resources.client.ResourceSharingClient

class ResourceSharingClientAccessorTests {

    private lateinit var accessor: ResourceSharingClientAccessor

    @BeforeEach
    fun setUp() {
        accessor = ResourceSharingClientAccessor.getInstance()
        // Reset state between tests
        accessor.setResourceSharingClient(null)
    }

    @Test
    fun `setResourceSharingClient accepts non-null client`() {
        val mockClient = mock(ResourceSharingClient::class.java)
        assertDoesNotThrow { accessor.setResourceSharingClient(mockClient) }
        assertEquals(mockClient, accessor.getResourceSharingClient())
    }

    @Test
    fun `setResourceSharingClient accepts null to clear the client`() {
        val mockClient = mock(ResourceSharingClient::class.java)
        accessor.setResourceSharingClient(mockClient)
        assertNotNull(accessor.getResourceSharingClient())

        assertDoesNotThrow { accessor.setResourceSharingClient(null) }
        assertNull(accessor.getResourceSharingClient())
    }

    @Test
    fun `assignResourceSharingClient does not throw NPE when called with null`() {
        // This test validates the fix for the NPE that crashes the master election loop.
        // When the resource-sharing feature flag is disabled, the security plugin calls
        // assignResourceSharingClient(null) on all registered extensions. Before the fix,
        // Kotlin's non-null parameter check would throw an NPE here.
        val extension = ReportsSchedulerExtension()

        // Simulate enable → disable cycle (exactly what the canary does)
        val mockClient = mock(ResourceSharingClient::class.java)
        assertDoesNotThrow { extension.assignResourceSharingClient(mockClient) }
        assertDoesNotThrow { extension.assignResourceSharingClient(null) }

        // After disable, client should be null
        assertNull(ResourceSharingClientAccessor.getInstance().getResourceSharingClient())
    }

    @Test
    fun `assignResourceSharingClient enables then disables without error`() {
        // Simulates repeated enable/disable cycles
        val extension = ReportsSchedulerExtension()
        val mockClient = mock(ResourceSharingClient::class.java)

        repeat(3) {
            assertDoesNotThrow { extension.assignResourceSharingClient(mockClient) }
            assertEquals(mockClient, ResourceSharingClientAccessor.getInstance().getResourceSharingClient())

            assertDoesNotThrow { extension.assignResourceSharingClient(null) }
            assertNull(ResourceSharingClientAccessor.getInstance().getResourceSharingClient())
        }
    }
}
