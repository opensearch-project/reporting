/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.index.ReportDefinitionsIndex
import org.opensearch.reportsscheduler.index.ReportInstancesIndex
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.GetAllReportInstancesRequest
import org.opensearch.reportsscheduler.model.GetAllReportInstancesResponse
import org.opensearch.reportsscheduler.model.GetReportInstanceRequest
import org.opensearch.reportsscheduler.model.GetReportInstanceResponse
import org.opensearch.reportsscheduler.model.InContextReportCreateRequest
import org.opensearch.reportsscheduler.model.InContextReportCreateResponse
import org.opensearch.reportsscheduler.model.OnDemandReportCreateRequest
import org.opensearch.reportsscheduler.model.OnDemandReportCreateResponse
import org.opensearch.reportsscheduler.model.ReportInstance
import org.opensearch.reportsscheduler.model.ReportInstance.Status
import org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusRequest
import org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusResponse
import org.opensearch.reportsscheduler.notifications.NotificationsActions
import org.opensearch.reportsscheduler.security.UserAccessManager
import org.opensearch.reportsscheduler.util.buildReportLink
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.rest.RestStatus
import java.time.Instant

/**
 * Report instances index operation actions.
 */
internal object ReportInstanceActions {
    private val log by logger(ReportInstanceActions::class.java)

    /**
     * Create a new on-demand report from in-context menu.
     * @param request [InContextReportCreateRequest] object
     * @return [InContextReportCreateResponse]
     */
    fun createOnDemand(request: InContextReportCreateRequest, user: User?): InContextReportCreateResponse {
        log.info("$LOG_PREFIX:ReportInstance-createOnDemand")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val reportInstance = ReportInstance("ignore",
            currentTime,
            currentTime,
            request.beginTime,
            request.endTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user),
            request.reportDefinitionDetails,
            Status.Success, // TODO: Revert to request.status when background job execution supported
            request.statusText,
            request.inContextDownloadUrlPath)
        val docId = ReportInstancesIndex.createReportInstance(reportInstance)
        docId ?: run {
            Metrics.REPORT_FROM_DEFINITION_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException("Report Instance Creation failed", RestStatus.INTERNAL_SERVER_ERROR)
        }
        val reportInstanceCopy = reportInstance.copy(id = docId)
        return InContextReportCreateResponse(reportInstanceCopy, true)
    }

    /**
     * Create on-demand report from report definition
     * @param request [OnDemandReportCreateRequest] object
     * @return [OnDemandReportCreateResponse]
     */
    fun createOnDemandFromDefinition(request: OnDemandReportCreateRequest, user: User?): OnDemandReportCreateResponse {
        log.info("$LOG_PREFIX:ReportInstance-createOnDemandFromDefinition ${request.reportDefinitionId}")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val reportDefinitionDetails = ReportDefinitionsIndex.getReportDefinition(request.reportDefinitionId)
        reportDefinitionDetails
            ?: run {
                Metrics.REPORT_DEFINITION_INFO_USER_ERROR_MISSING_REPORT_DEF_DETAILS.counter.increment()
                throw OpenSearchStatusException("Report Definition ${request.reportDefinitionId} not found", RestStatus.NOT_FOUND)
            }

        if (!UserAccessManager.doesUserHasAccess(user, reportDefinitionDetails.tenant, reportDefinitionDetails.access)) {
            Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException("Permission denied for Report Definition ${request.reportDefinitionId}", RestStatus.FORBIDDEN)
        }
        val beginTime: Instant = currentTime.minus(reportDefinitionDetails.reportDefinition.format.duration)
        val endTime: Instant = currentTime
        val currentStatus: Status = Status.Success // TODO: Revert to Executing when background job execution supported
        val reportInstance = ReportInstance("ignore",
            currentTime,
            currentTime,
            beginTime,
            endTime,
            UserAccessManager.getUserTenant(user),
            reportDefinitionDetails.access,
            reportDefinitionDetails,
            currentStatus)
        val docId = ReportInstancesIndex.createReportInstance(reportInstance)
        docId ?: run {
            Metrics.REPORT_FROM_DEFINITION_ID_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException("Report Instance Creation failed", RestStatus.INTERNAL_SERVER_ERROR)
        }
        if (reportDefinitionDetails.reportDefinition.delivery != null) {
            val reportLink = buildReportLink(reportDefinitionDetails.reportDefinition.source.origin, reportInstance.tenant, docId)
            NotificationsActions.send(reportDefinitionDetails.reportDefinition.delivery, docId, reportLink)
        }
        val reportInstanceCopy = reportInstance.copy(id = docId)
        return OnDemandReportCreateResponse(reportInstanceCopy, true)
    }

    /**
     * Update status of existing report instance
     * @param request [UpdateReportInstanceStatusRequest] object
     * @return [UpdateReportInstanceStatusResponse]
     */
    fun update(request: UpdateReportInstanceStatusRequest, user: User?): UpdateReportInstanceStatusResponse {
        log.info("$LOG_PREFIX:ReportInstance-update ${request.reportInstanceId}")
        UserAccessManager.validateUser(user)
        val currentReportInstance = ReportInstancesIndex.getReportInstance(request.reportInstanceId)
        currentReportInstance
            ?: run {
                Metrics.REPORT_INSTANCE_UPDATE_USER_ERROR_MISSING_REPORT_INSTANCE.counter.increment()
                throw OpenSearchStatusException("Report Instance ${request.reportInstanceId} not found", RestStatus.NOT_FOUND)
            }
        if (!UserAccessManager.doesUserHasAccess(user, currentReportInstance.tenant, currentReportInstance.access)) {
            Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException("Permission denied for Report Definition ${request.reportInstanceId}", RestStatus.FORBIDDEN)
        }
        if (request.status == Status.Scheduled) { // Don't allow changing status to Scheduled
            Metrics.REPORT_INSTANCE_UPDATE_USER_ERROR_INVALID_STATUS.counter.increment()
            throw OpenSearchStatusException("Status cannot be updated to ${Status.Scheduled}", RestStatus.BAD_REQUEST)
        }
        val currentTime = Instant.now()
        val updatedReportInstance = currentReportInstance.copy(updatedTime = currentTime,
            status = request.status,
            statusText = request.statusText)
        if (!ReportInstancesIndex.updateReportInstance(updatedReportInstance)) {
            Metrics.REPORT_INSTANCE_UPDATE_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException("Report Instance state update failed", RestStatus.INTERNAL_SERVER_ERROR)
        }
        return UpdateReportInstanceStatusResponse(request.reportInstanceId)
    }

    /**
     * Get information of existing report instance
     * @param request [GetReportInstanceRequest] object
     * @return [GetReportInstanceResponse]
     */
    fun info(request: GetReportInstanceRequest, user: User?): GetReportInstanceResponse {
        log.info("$LOG_PREFIX:ReportInstance-info ${request.reportInstanceId}")
        UserAccessManager.validateUser(user)
        val reportInstance = ReportInstancesIndex.getReportInstance(request.reportInstanceId)
        reportInstance
            ?: run {
                Metrics.REPORT_INSTANCE_INFO_USER_ERROR_MISSING_REPORT_INSTANCE.counter.increment()
                throw OpenSearchStatusException("Report Instance ${request.reportInstanceId} not found", RestStatus.NOT_FOUND)
            }

        if (!UserAccessManager.doesUserHasAccess(user, reportInstance.tenant, reportInstance.access)) {
            Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException("Permission denied for Report Definition ${request.reportInstanceId}", RestStatus.FORBIDDEN)
        }
        return GetReportInstanceResponse(reportInstance, true)
    }

    /**
     * Get information of all report instances
     * @param request [GetAllReportInstancesRequest] object
     * @return [GetAllReportInstancesResponse]
     */
    fun getAll(request: GetAllReportInstancesRequest, user: User?): GetAllReportInstancesResponse {
        log.info("$LOG_PREFIX:ReportInstance-getAll fromIndex:${request.fromIndex} maxItems:${request.maxItems}")
        UserAccessManager.validateUser(user)
        val reportInstanceList = ReportInstancesIndex.getAllReportInstances(UserAccessManager.getUserTenant(user),
            UserAccessManager.getSearchAccessInfo(user),
            request.fromIndex,
            request.maxItems)
        return GetAllReportInstancesResponse(reportInstanceList, true)
    }
}
