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
import org.opensearch.reportsscheduler.model.PollReportInstanceResponse
import org.opensearch.reportsscheduler.model.ReportInstance
import org.opensearch.reportsscheduler.model.ReportInstance.Status
import org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusRequest
import org.opensearch.reportsscheduler.model.UpdateReportInstanceStatusResponse
import org.opensearch.reportsscheduler.security.UserAccessManager
import org.opensearch.reportsscheduler.settings.PluginSettings
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.OpenSearchStatusException
import org.opensearch.rest.RestStatus
import java.time.Instant
import kotlin.random.Random

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
        return InContextReportCreateResponse(reportInstanceCopy, UserAccessManager.hasAllInfoAccess(user))
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
        val reportInstanceCopy = reportInstance.copy(id = docId)
        return OnDemandReportCreateResponse(reportInstanceCopy, UserAccessManager.hasAllInfoAccess(user))
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
        return GetReportInstanceResponse(reportInstance, UserAccessManager.hasAllInfoAccess(user))
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
        return GetAllReportInstancesResponse(reportInstanceList, UserAccessManager.hasAllInfoAccess(user))
    }

    fun poll(user: User?): PollReportInstanceResponse {
        log.info("$LOG_PREFIX:ReportInstance-poll")
        UserAccessManager.validatePollingUser(user)
        val currentTime = Instant.now()
        val reportInstances = ReportInstancesIndex.getPendingReportInstances()
        return if (reportInstances.isEmpty()) {
            PollReportInstanceResponse(getRetryAfterTime())
        } else {
            // Shuffle list so that when multiple requests are made, chances of lock conflict is less
            reportInstances.shuffle()
            /*
            If the shuffling is perfect random then there is high probability that first item locking is successful
            even when there are many parallel requests. i.e. say there are x jobs and y parallel requests.
            then x out of y jobs can lock first item and rest cannot lock any jobs. However shuffle may not be perfect
            hence checking first few jobs for locking.
            */
            val lockedJob = reportInstances.subList(0, PluginSettings.maxLockRetries).find {
                val updatedInstance = it.copy(reportInstance = it.reportInstance.copy(
                    updatedTime = currentTime,
                    status = Status.Executing
                ))
                ReportInstancesIndex.updateReportInstanceDoc(updatedInstance)
            }
            if (lockedJob == null) {
                PollReportInstanceResponse(PluginSettings.minPollingDurationSeconds)
            } else {
                PollReportInstanceResponse(0, lockedJob.reportInstance, UserAccessManager.hasAllInfoAccess(user))
            }
        }
    }

    private fun getRetryAfterTime(): Int {
        return Random.nextInt(PluginSettings.minPollingDurationSeconds, PluginSettings.maxPollingDurationSeconds)
    }
}
