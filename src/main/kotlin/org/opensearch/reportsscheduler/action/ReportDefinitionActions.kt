/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.action

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.index.ReportDefinitionsIndex
import org.opensearch.reportsscheduler.metrics.Metrics
import org.opensearch.reportsscheduler.model.CreateReportDefinitionRequest
import org.opensearch.reportsscheduler.model.CreateReportDefinitionResponse
import org.opensearch.reportsscheduler.model.DeleteReportDefinitionRequest
import org.opensearch.reportsscheduler.model.DeleteReportDefinitionResponse
import org.opensearch.reportsscheduler.model.GetAllReportDefinitionsRequest
import org.opensearch.reportsscheduler.model.GetAllReportDefinitionsResponse
import org.opensearch.reportsscheduler.model.GetReportDefinitionRequest
import org.opensearch.reportsscheduler.model.GetReportDefinitionResponse
import org.opensearch.reportsscheduler.model.ReportDefinitionDetails
import org.opensearch.reportsscheduler.model.UpdateReportDefinitionRequest
import org.opensearch.reportsscheduler.model.UpdateReportDefinitionResponse
import org.opensearch.reportsscheduler.security.UserAccessManager
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.rest.RestStatus
import java.time.Instant

/**
 * Report definitions index operation actions.
 */
internal object ReportDefinitionActions {
    private val log by logger(ReportDefinitionActions::class.java)

    /**
     * Create new ReportDefinition
     * @param request [CreateReportDefinitionRequest] object
     * @return [CreateReportDefinitionResponse]
     */
    fun create(request: CreateReportDefinitionRequest, user: User?): CreateReportDefinitionResponse {
        log.info("$LOG_PREFIX:ReportDefinition-create")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val reportDefinitionDetails = ReportDefinitionDetails(
            "ignore",
            currentTime,
            currentTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user),
            request.reportDefinition
        )
        val docId = ReportDefinitionsIndex.createReportDefinition(reportDefinitionDetails)
        docId ?: throw OpenSearchStatusException(
            "Report Definition Creation failed",
            RestStatus.INTERNAL_SERVER_ERROR
        )
        return CreateReportDefinitionResponse(docId)
    }

    /**
     * Update ReportDefinition
     * @param request [UpdateReportDefinitionRequest] object
     * @return [UpdateReportDefinitionResponse]
     */
    fun update(request: UpdateReportDefinitionRequest, user: User?): UpdateReportDefinitionResponse {
        log.info("$LOG_PREFIX:ReportDefinition-update ${request.reportDefinitionId}")
        UserAccessManager.validateUser(user)
        val currentReportDefinitionDetails = ReportDefinitionsIndex.getReportDefinition(request.reportDefinitionId)
        currentReportDefinitionDetails
            ?: run {
                Metrics.REPORT_DEFINITION_UPDATE_USER_ERROR_MISSING_REPORT_DEF_DETAILS.counter.increment()
                throw OpenSearchStatusException("Report Definition ${request.reportDefinitionId} not found", RestStatus.NOT_FOUND)
            }

        if (!UserAccessManager.doesUserHasAccess(user, currentReportDefinitionDetails.tenant, currentReportDefinitionDetails.access)) {
            Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException("Permission denied for Report Definition ${request.reportDefinitionId}", RestStatus.FORBIDDEN)
        }
        val currentTime = Instant.now()
        val reportDefinitionDetails = ReportDefinitionDetails(
            request.reportDefinitionId,
            currentTime,
            currentReportDefinitionDetails.createdTime,
            UserAccessManager.getUserTenant(user),
            currentReportDefinitionDetails.access,
            request.reportDefinition
        )
        if (!ReportDefinitionsIndex.updateReportDefinition(request.reportDefinitionId, reportDefinitionDetails)) {
            Metrics.REPORT_DEFINITION_UPDATE_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException("Report Definition Update failed", RestStatus.INTERNAL_SERVER_ERROR)
        }
        return UpdateReportDefinitionResponse(request.reportDefinitionId)
    }

    /**
     * Get ReportDefinition info
     * @param request [GetReportDefinitionRequest] object
     * @return [GetReportDefinitionResponse]
     */
    fun info(request: GetReportDefinitionRequest, user: User?): GetReportDefinitionResponse {
        log.info("$LOG_PREFIX:ReportDefinition-info ${request.reportDefinitionId}")
        UserAccessManager.validateUser(user)
        val reportDefinitionDetails = ReportDefinitionsIndex.getReportDefinition(request.reportDefinitionId)
        reportDefinitionDetails
            ?: run {
                Metrics.REPORT_DEFINITION_INFO_SYSTEM_ERROR.counter.increment()
                throw OpenSearchStatusException("Report Definition ${request.reportDefinitionId} not found", RestStatus.NOT_FOUND)
            }

        if (!UserAccessManager.doesUserHasAccess(user, reportDefinitionDetails.tenant, reportDefinitionDetails.access)) {
            Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException("Permission denied for Report Definition ${request.reportDefinitionId}", RestStatus.FORBIDDEN)
        }
        return GetReportDefinitionResponse(reportDefinitionDetails, true)
    }

    /**
     * Delete ReportDefinition
     * @param request [DeleteReportDefinitionRequest] object
     * @return [DeleteReportDefinitionResponse]
     */
    fun delete(request: DeleteReportDefinitionRequest, user: User?): DeleteReportDefinitionResponse {
        log.info("$LOG_PREFIX:ReportDefinition-delete ${request.reportDefinitionId}")
        UserAccessManager.validateUser(user)
        val reportDefinitionDetails = ReportDefinitionsIndex.getReportDefinition(request.reportDefinitionId)
        reportDefinitionDetails
            ?: run {
                Metrics.REPORT_DEFINITION_DELETE_USER_ERROR_MISSING_REPORT_DEF_DETAILS.counter.increment()
                throw OpenSearchStatusException("Report Definition ${request.reportDefinitionId} not found", RestStatus.NOT_FOUND)
            }

        if (!UserAccessManager.doesUserHasAccess(user, reportDefinitionDetails.tenant, reportDefinitionDetails.access)) {
            Metrics.REPORT_PERMISSION_USER_ERROR.counter.increment()
            throw OpenSearchStatusException("Permission denied for Report Definition ${request.reportDefinitionId}", RestStatus.FORBIDDEN)
        }
        if (!ReportDefinitionsIndex.deleteReportDefinition(request.reportDefinitionId)) {
            Metrics.REPORT_DEFINITION_DELETE_SYSTEM_ERROR.counter.increment()
            throw OpenSearchStatusException("Report Definition ${request.reportDefinitionId} delete failed", RestStatus.REQUEST_TIMEOUT)
        }
        return DeleteReportDefinitionResponse(request.reportDefinitionId)
    }

    /**
     * Get all ReportDefinitions
     * @param request [GetAllReportDefinitionsRequest] object
     * @return [GetAllReportDefinitionsResponse]
     */
    fun getAll(request: GetAllReportDefinitionsRequest, user: User?): GetAllReportDefinitionsResponse {
        log.info("$LOG_PREFIX:ReportDefinition-getAll fromIndex:${request.fromIndex} maxItems:${request.maxItems}")
        UserAccessManager.validateUser(user)
        val reportDefinitionsList = ReportDefinitionsIndex.getAllReportDefinitions(
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getSearchAccessInfo(user),
            request.fromIndex,
            request.maxItems
        )
        return GetAllReportDefinitionsResponse(reportDefinitionsList, true)
    }
}
