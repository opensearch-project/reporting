/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler

import org.opensearch.reportsscheduler.index.ReportDefinitionsIndex
import org.opensearch.reportsscheduler.index.ReportInstancesIndex
import org.opensearch.reportsscheduler.resources.ResourceSharingClientAccessor
import org.opensearch.reportsscheduler.resources.Utils
import org.opensearch.security.spi.resources.ResourceProvider
import org.opensearch.security.spi.resources.ResourceSharingExtension
import org.opensearch.security.spi.resources.client.ResourceSharingClient

class ReportsSchedulerExtension : ResourceSharingExtension {

    override fun getResourceProviders(): Set<ResourceProvider> {
        return setOf(
            object : ResourceProvider {
                override fun resourceType(): String = Utils.REPORT_DEFINITION_TYPE
                override fun resourceIndexName(): String = ReportDefinitionsIndex.REPORT_DEFINITIONS_INDEX_NAME
            },
            object : ResourceProvider {
                override fun resourceType(): String = Utils.REPORT_INSTANCE_TYPE
                override fun resourceIndexName(): String = ReportInstancesIndex.REPORT_INSTANCES_INDEX_NAME
            }
        )
    }

    override fun assignResourceSharingClient(resourceSharingClient: ResourceSharingClient) {
        ResourceSharingClientAccessor.getInstance()
            .setResourceSharingClient(resourceSharingClient)
    }
}
