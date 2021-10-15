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

package org.opensearch.reportsscheduler.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opensearch.jobscheduler.spi.JobExecutionContext
import org.opensearch.jobscheduler.spi.ScheduledJobParameter
import org.opensearch.jobscheduler.spi.ScheduledJobRunner
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.index.ReportInstancesIndex
import org.opensearch.reportsscheduler.model.ReportDefinitionDetails
import org.opensearch.reportsscheduler.model.ReportInstance
import org.opensearch.reportsscheduler.notifications.NotificationsActions
import org.opensearch.reportsscheduler.security.UserAccessManager
import org.opensearch.reportsscheduler.util.buildReportLink
import org.opensearch.reportsscheduler.util.logger
import java.time.Instant

internal object ReportDefinitionJobRunner : ScheduledJobRunner {
    private val log by logger(ReportDefinitionJobRunner::class.java)
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun runJob(job: ScheduledJobParameter, context: JobExecutionContext) {
        if (job !is ReportDefinitionDetails) {
            log.warn("$LOG_PREFIX:job is not of type ReportDefinitionDetails:${job.javaClass.name}")
            throw IllegalArgumentException("job is not of type ReportDefinitionDetails:${job.javaClass.name}")
        }
        scope.launch {
            val reportDefinitionDetails: ReportDefinitionDetails = job
            val currentTime = Instant.now()
            val endTime = context.expectedExecutionTime
            val beginTime = endTime.minus(reportDefinitionDetails.reportDefinition.format.duration)
            val reportInstance = ReportInstance(context.jobId,
                currentTime,
                currentTime,
                beginTime,
                endTime,
                job.tenant,
                job.access,
                reportDefinitionDetails,
                ReportInstance.Status.Success) // TODO: Revert to Scheduled when background job execution supported
            val id = ReportInstancesIndex.createReportInstance(reportInstance)
            if (id == null) {
                log.warn("$LOG_PREFIX:runJob-job creation failed for $reportInstance")
            } else {
                log.info("$LOG_PREFIX:runJob-created job:$id")
                if (reportDefinitionDetails.reportDefinition.delivery != null) {
                    val user = UserAccessManager.getUserFromAccess(job.access)
                    val userStr = user?.let { it.toString() } ?: ""
                    val reportLink = buildReportLink(reportDefinitionDetails.reportDefinition.source.origin, reportInstance.tenant, id)
                    NotificationsActions.send(reportDefinitionDetails.reportDefinition.delivery, id, reportLink, userStr)
                }
            }
        }
    }
}
