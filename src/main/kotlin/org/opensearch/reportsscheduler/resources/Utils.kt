/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.resources

internal object Utils {
    const val REPORT_DEFINITION_TYPE = "report-definition"

    const val REPORT_INSTANCE_TYPE = "report-instance"

    fun shouldUseResourceAuthz(resourceType: String): Boolean {
        return ResourceSharingClientAccessor.getInstance()
            .getResourceSharingClient()
            ?.isFeatureEnabledForType(resourceType)
            ?: false
    }
}
