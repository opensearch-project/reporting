/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.reportsscheduler.index

import org.opensearch.ResourceAlreadyExistsException
import org.opensearch.action.DocWriteResponse
import org.opensearch.action.admin.indices.create.CreateIndexRequest
import org.opensearch.action.delete.DeleteRequest
import org.opensearch.action.get.GetRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.update.UpdateRequest
import org.opensearch.client.Client
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentType
import org.opensearch.index.query.QueryBuilders
import org.opensearch.reportsscheduler.ReportsSchedulerPlugin.Companion.LOG_PREFIX
import org.opensearch.reportsscheduler.model.ReportInstance
import org.opensearch.reportsscheduler.model.ReportInstanceDoc
import org.opensearch.reportsscheduler.model.ReportInstanceSearchResults
import org.opensearch.reportsscheduler.model.RestTag.ACCESS_LIST_FIELD
import org.opensearch.reportsscheduler.model.RestTag.TENANT_FIELD
import org.opensearch.reportsscheduler.model.RestTag.UPDATED_TIME_FIELD
import org.opensearch.reportsscheduler.settings.PluginSettings
import org.opensearch.reportsscheduler.util.SecureIndexClient
import org.opensearch.reportsscheduler.util.logger
import org.opensearch.search.builder.SearchSourceBuilder
import java.util.concurrent.TimeUnit

/**
 * Class for doing ES index operation to maintain report instances in cluster.
 */
internal object ReportInstancesIndex {
    private val log by logger(ReportInstancesIndex::class.java)
    private const val REPORT_INSTANCES_INDEX_NAME = ".opendistro-reports-instances"
    private const val REPORT_INSTANCES_MAPPING_FILE_NAME = "report-instances-mapping.yml"
    private const val REPORT_INSTANCES_SETTINGS_FILE_NAME = "report-instances-settings.yml"
    private const val MAPPING_TYPE = "_doc"

    private lateinit var client: Client
    private lateinit var clusterService: ClusterService

    /**
     * Initialize the class
     * @param client The ES client
     * @param clusterService The ES cluster service
     */
    fun initialize(client: Client, clusterService: ClusterService) {
        this.client = SecureIndexClient(client)
        this.clusterService = clusterService
    }

    /**
     * Create index using the mapping and settings defined in resource
     */
    @Suppress("TooGenericExceptionCaught")
    private fun createIndex() {
        if (!isIndexExists()) {
            val classLoader = ReportInstancesIndex::class.java.classLoader
            val indexMappingSource = classLoader.getResource(REPORT_INSTANCES_MAPPING_FILE_NAME)?.readText()!!
            val indexSettingsSource = classLoader.getResource(REPORT_INSTANCES_SETTINGS_FILE_NAME)?.readText()!!
            val request = CreateIndexRequest(REPORT_INSTANCES_INDEX_NAME)
                .mapping(indexMappingSource, XContentType.YAML)
                .settings(indexSettingsSource, XContentType.YAML)
            try {
                val actionFuture = client.admin().indices().create(request)
                val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                if (response.isAcknowledged) {
                    log.info("$LOG_PREFIX:Index $REPORT_INSTANCES_INDEX_NAME creation Acknowledged")
                } else {
                    throw IllegalStateException("$LOG_PREFIX:Index $REPORT_INSTANCES_INDEX_NAME creation not Acknowledged")
                }
            } catch (exception: Exception) {
                if (exception !is ResourceAlreadyExistsException && exception.cause !is ResourceAlreadyExistsException) {
                    throw exception
                }
            }
        }
    }

    /**
     * Check if the index is created and available.
     * @return true if index is available, false otherwise
     */
    private fun isIndexExists(): Boolean {
        val clusterState = clusterService.state()
        return clusterState.routingTable.hasIndex(REPORT_INSTANCES_INDEX_NAME)
    }

    /**
     * create a new doc for reportInstance
     * @param reportInstance the report instance
     * @return ReportInstance.id if successful, null otherwise
     * @throws java.util.concurrent.ExecutionException with a cause
     */
    fun createReportInstance(reportInstance: ReportInstance): String? {
        createIndex()
        val indexRequest = IndexRequest(REPORT_INSTANCES_INDEX_NAME)
            .source(reportInstance.toXContent())
            .create(true)
        val actionFuture = client.index(indexRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return if (response.result != DocWriteResponse.Result.CREATED) {
            log.warn("$LOG_PREFIX:createReportInstance - response:$response")
            null
        } else {
            response.id
        }
    }

    /**
     * Query index for report instance ID
     * @param id the id for the document
     * @return Report instance details on success, null otherwise
     */
    fun getReportInstance(id: String): ReportInstance? {
        createIndex()
        val getRequest = GetRequest(REPORT_INSTANCES_INDEX_NAME).id(id)
        val actionFuture = client.get(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return if (response.sourceAsString == null) {
            log.warn("$LOG_PREFIX:getReportInstance - $id not found; response:$response")
            null
        } else {
            val parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                response.sourceAsString)
            parser.nextToken()
            ReportInstance.parse(parser, id)
        }
    }

    /**
     * Query index for report instance for given access details
     * @param tenant the tenant of the user
     * @param access the list of access details to search reports for.
     * @param from the paginated start index
     * @param maxItems the max items to query
     * @return search result of Report instance details
     */
    fun getAllReportInstances(tenant: String, access: List<String>, from: Int, maxItems: Int): ReportInstanceSearchResults {
        createIndex()
        val sourceBuilder = SearchSourceBuilder()
            .timeout(TimeValue(PluginSettings.operationTimeoutMs, TimeUnit.MILLISECONDS))
            .sort(UPDATED_TIME_FIELD)
            .size(maxItems)
            .from(from)
        val tenantQuery = QueryBuilders.termsQuery(TENANT_FIELD, tenant)
        if (access.isNotEmpty()) {
            val accessQuery = QueryBuilders.termsQuery(ACCESS_LIST_FIELD, access)
            val query = QueryBuilders.boolQuery()
            query.filter(tenantQuery)
            query.filter(accessQuery)
            sourceBuilder.query(query)
        } else {
            sourceBuilder.query(tenantQuery)
        }
        val searchRequest = SearchRequest()
            .indices(REPORT_INSTANCES_INDEX_NAME)
            .source(sourceBuilder)
        val actionFuture = client.search(searchRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val result = ReportInstanceSearchResults(from.toLong(), response)
        log.info("$LOG_PREFIX:getAllReportInstances from:$from, maxItems:$maxItems," +
            " retCount:${result.objectList.size}, totalCount:${result.totalHits}")
        return result
    }

    /**
     * update Report instance details for given id
     * @param reportInstance the Report instance details data
     * @return true if successful, false otherwise
     */
    fun updateReportInstance(reportInstance: ReportInstance): Boolean {
        createIndex()
        val updateRequest = UpdateRequest()
            .index(REPORT_INSTANCES_INDEX_NAME)
            .id(reportInstance.id)
            .doc(reportInstance.toXContent())
            .fetchSource(true)
        val actionFuture = client.update(updateRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.UPDATED) {
            log.warn("$LOG_PREFIX:updateReportInstance failed for ${reportInstance.id}; response:$response")
        }
        return response.result == DocWriteResponse.Result.UPDATED
    }

    /**
     * update Report instance details for given id
     * @param reportInstanceDoc the Report instance details doc data
     * @return true if successful, false otherwise
     */
    fun updateReportInstanceDoc(reportInstanceDoc: ReportInstanceDoc): Boolean {
        createIndex()
        val updateRequest = UpdateRequest()
            .index(REPORT_INSTANCES_INDEX_NAME)
            .id(reportInstanceDoc.reportInstance.id)
            .setIfSeqNo(reportInstanceDoc.seqNo)
            .setIfPrimaryTerm(reportInstanceDoc.primaryTerm)
            .doc(reportInstanceDoc.reportInstance.toXContent())
            .fetchSource(true)
        val actionFuture = client.update(updateRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.UPDATED) {
            log.warn("$LOG_PREFIX:updateReportInstanceDoc failed for ${reportInstanceDoc.reportInstance.id}; response:$response")
        }
        return response.result == DocWriteResponse.Result.UPDATED
    }

    /**
     * delete Report instance details for given id
     * @param id the id for the document
     * @return true if successful, false otherwise
     */
    fun deleteReportInstance(id: String): Boolean {
        createIndex()
        val deleteRequest = DeleteRequest()
            .index(REPORT_INSTANCES_INDEX_NAME)
            .id(id)
        val actionFuture = client.delete(deleteRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.DELETED) {
            log.warn("$LOG_PREFIX:deleteReportInstance failed for $id; response:$response")
        }
        return response.result == DocWriteResponse.Result.DELETED
    }
}
