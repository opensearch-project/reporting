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

                // Report instances are child resources of the report definition
                // they were generated from: access is inherited from the parent
                // definition's sharing record. This also covers instances created
                // without an authenticated user in context (on-demand runs under
                // the plugin subject; scheduled runs under job-scheduler).
                override fun parentType(): String = Utils.REPORT_DEFINITION_TYPE
                override fun parentIdField(): String = PARENT_ID_FIELD
            }
        )
    }

    companion object {
        // Flattened field in the report instance document that holds the id of
        // the report definition it was generated from. Indexed as keyword in
        // report-instances-mapping.yml.
        private const val PARENT_ID_FIELD = "reportDefinitionDetails.id"
    }

    override fun assignResourceSharingClient(resourceSharingClient: ResourceSharingClient?) {
        ResourceSharingClientAccessor.getInstance()
            .setResourceSharingClient(resourceSharingClient)
    }
}
