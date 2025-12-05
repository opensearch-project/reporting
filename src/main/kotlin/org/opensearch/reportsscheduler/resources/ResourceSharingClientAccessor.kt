/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.resources

import org.opensearch.security.spi.resources.client.ResourceSharingClient

class ResourceSharingClientAccessor private constructor() {

    private var client: ResourceSharingClient? = null

    fun setResourceSharingClient(client: ResourceSharingClient) {
        this.client = client
    }

    fun getResourceSharingClient(): ResourceSharingClient? {
        return client
    }

    companion object {
        @Volatile
        private var instance: ResourceSharingClientAccessor? = null

        fun getInstance(): ResourceSharingClientAccessor {
            return instance ?: synchronized(this) {
                instance ?: ResourceSharingClientAccessor().also { instance = it }
            }
        }
    }
}
