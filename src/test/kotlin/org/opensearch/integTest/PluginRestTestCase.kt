/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.integTest

import com.google.gson.JsonObject
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.HttpHost
import org.awaitility.Awaitility
import org.hamcrest.Matchers
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.opensearch.client.Request
import org.opensearch.client.RequestOptions
import org.opensearch.client.Response
import org.opensearch.client.ResponseException
import org.opensearch.client.RestClient
import org.opensearch.client.WarningFailureException
import org.opensearch.client.WarningsHandler
import org.opensearch.common.io.PathUtils
import org.opensearch.common.settings.Settings
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.commons.ConfigConstants
import org.opensearch.commons.rest.SecureRestClientBuilder
import org.opensearch.core.xcontent.DeprecationHandler
import org.opensearch.core.xcontent.MediaType
import org.opensearch.core.xcontent.NamedXContentRegistry
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.test.rest.OpenSearchRestTestCase
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.Locale
import javax.management.MBeanServerInvocationHandler
import javax.management.ObjectName
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

abstract class PluginRestTestCase : OpenSearchRestTestCase() {

    protected fun isHttps(): Boolean {
        return System.getProperty("https", "false")!!.toBoolean()
    }

    override fun getProtocol(): String {
        return if (isHttps()) {
            "https"
        } else {
            "http"
        }
    }

    /**
     * wipeAllIndices won't work since it cannot delete security index. Use wipeAllODFEIndices instead.
     */
    override fun preserveIndicesUponCompletion(): Boolean {
        return true
    }

    open fun preserveODFEIndicesAfterTest(): Boolean = false

    protected lateinit var clusterHosts: Array<HttpHost>

    protected val reportFullRole = "reports_full_access"
    protected val reportReadRole = "reports_read_access"
    protected val reportNoAccessRole = "reports_no_access"

    protected val reportFullUser = "reports_full_user"
    protected val reportReadUser = "reports_read_user"
    protected val reportNoAccessUser = "reports_no_access_user"

    protected lateinit var reportsFullClient: RestClient
    protected lateinit var reportsReadClient: RestClient
    protected lateinit var reportsNoAccessClient: RestClient

    protected val reportReadOnlyAccessLevel: String = "rd_read_only"
    protected val reportFullAccessLevel: String = "rd_full_access"

    protected val reportInstanceReadOnlyAccessLevel: String = "ri_read_only"
    protected val reportInstanceFullAccessLevel: String = "ri_full_access"

    val shareConfigUri = "/_plugins/_security/api/resource/share"

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        if (isHttps()) {
            initReportSecurityUsersAndRoles()
        }
    }

    @Throws(IOException::class)
    @After
    open fun wipeAllODFEIndices() {
        if (preserveODFEIndicesAfterTest()) return
        val response = client().performRequest(Request("GET", "/_cat/indices?format=json&expand_wildcards=all"))
        val xContentType = MediaType.fromMediaType(response.entity.contentType)
        xContentType.xContent().createParser(
            NamedXContentRegistry.EMPTY,
            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            response.entity.content
        ).use { parser ->
            for (index in parser.list()) {
                val jsonObject: Map<*, *> = index as java.util.HashMap<*, *>
                val indexName: String = jsonObject["index"] as String
                if (!indexName.startsWith(".kibana") && !indexName.startsWith(".opendistro_security")) {
                    val request = Request("DELETE", "/$indexName")
                    // TODO: remove PERMISSIVE option after moving system index access to REST API call
                    val options = RequestOptions.DEFAULT.toBuilder()
                    options.setWarningsHandler(WarningsHandler.PERMISSIVE)
                    request.options = options.build()
                    adminClient().performRequest(request)
                }
            }
        }
    }

    @After
    open fun wipeAllSettings() {
        wipeAllClusterSettings()
    }

    @After
    open fun cleanupReportSecurity() {
        try {
            if (this::reportsFullClient.isInitialized) {
                try {
                    reportsFullClient.close()
                } catch (_: Exception) {
                }
            }
            if (this::reportsReadClient.isInitialized) {
                try {
                    reportsReadClient.close()
                } catch (_: Exception) {
                }
            }
            if (this::reportsNoAccessClient.isInitialized) {
                try {
                    reportsNoAccessClient.close()
                } catch (_: Exception) {
                }
            }
            if (isHttps()) {
                deleteReportSecurityUsers()
            }
        } catch (_: Exception) {
        }
    }

    override fun restAdminSettings(): Settings {
        return Settings
            .builder()
            .put("http.port", 9200)
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_ENABLED, isHttps())
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_PEMCERT_FILEPATH, "sample.pem")
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH, "test-kirk.jks")
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_PASSWORD, "changeit")
            .put(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_KEYPASSWORD, "changeit")
            .build()
    }

    @Throws(IOException::class)
    override fun buildClient(settings: Settings, hosts: Array<HttpHost>): RestClient {
        if (isHttps()) {
            val keystore = settings.get(ConfigConstants.OPENSEARCH_SECURITY_SSL_HTTP_KEYSTORE_FILEPATH)
            return when (keystore != null) {
                true -> {
                    // create adminDN (super-admin) client
                    val uri = javaClass.classLoader.getResource("security/sample.pem").toURI()
                    val configPath = PathUtils.get(uri).parent.toAbsolutePath()
                    SecureRestClientBuilder(settings, configPath, hosts).setSocketTimeout(60000).build()
                }
                false -> {
                    // create client with passed user
                    val userName = System.getProperty("user")
                    val password = System.getProperty("password")
                    SecureRestClientBuilder(hosts, isHttps(), userName, password).setSocketTimeout(60000).build()
                }
            }
        } else {
            val builder = RestClient.builder(*hosts)
            configureClient(builder, settings)
            builder.setStrictDeprecationMode(true)
            return builder.build()
        }
    }

    fun executeRequest(
        method: String,
        url: String,
        jsonString: String,
        expectedRestStatus: Int? = null
    ): JsonObject {
        val request = Request(method, url)
        request.setJsonEntity(jsonString)
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(client(), request, expectedRestStatus)
    }

    fun executeRequest(
        restClient: RestClient,
        method: String,
        url: String,
        jsonString: String,
        expectedRestStatus: Int? = null
    ): JsonObject {
        val request = Request(method, url)
        request.setJsonEntity(jsonString)
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(restClient, request, expectedRestStatus)
    }

    private fun executeRequest(request: Request, expectedRestStatus: Int? = null): JsonObject {
        return executeRequest(client(), request, expectedRestStatus)
    }

    private fun executeRequest(restClient: RestClient, request: Request, expectedRestStatus: Int? = null): JsonObject {
        val response = try {
            restClient.performRequest(request)
        } catch (exception: ResponseException) {
            exception.response
        } catch (exception: WarningFailureException) {
            exception.response
        }
        if (expectedRestStatus != null) {
            assertEquals(expectedRestStatus, response.statusLine.statusCode)
        }
        val responseBody = getResponseBody(response)
        return jsonify(responseBody)
    }

    @Throws(IOException::class)
    private fun getResponseBody(response: Response, retainNewLines: Boolean = true): String {
        val sb = StringBuilder()
        response.entity.content.use { `is` ->
            BufferedReader(
                InputStreamReader(`is`, StandardCharsets.UTF_8)
            ).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    sb.append(line)
                    if (retainNewLines) {
                        sb.appendLine()
                    }
                }
            }
        }
        return sb.toString()
    }

    @Throws(IOException::class)
    protected open fun getAllClusterSettings(): JsonObject? {
        val request = Request("GET", "/_cluster/settings?flat_settings&include_defaults")
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(request)
    }

    @Throws(IOException::class)
    protected fun updateClusterSettings(setting: ClusterSetting): JsonObject {
        val request = Request("PUT", "/_cluster/settings")
        val persistentSetting = "{\"${setting.type}\": {\"${setting.name}\": ${setting.value}}}"
        request.setJsonEntity(persistentSetting)
        val restOptionsBuilder = RequestOptions.DEFAULT.toBuilder()
        restOptionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(restOptionsBuilder)
        return executeRequest(request)
    }

    @Throws(IOException::class)
    protected open fun wipeAllClusterSettings() {
        updateClusterSettings(ClusterSetting("persistent", "*", null))
        updateClusterSettings(ClusterSetting("transient", "*", null))
    }

    protected class ClusterSetting(val type: String, val name: String, var value: Any?) {
        init {
            this.value = if (value == null) "null" else "\"" + value + "\""
        }
    }

    protected fun generatePassword(username: String): String = "${username}_pwd"

    @Throws(IOException::class)
    protected fun createUser(username: String, password: String, backendRoles: List<String> = emptyList()) {
        val backendRolesJson =
            if (backendRoles.isEmpty()) {
                "[]"
            } else {
                backendRoles.joinToString(prefix = "[\"", postfix = "\"]", separator = "\", \"")
            }
        val body = """
            {
              "password": "$password",
              "backend_roles": $backendRolesJson,
              "attributes": {}
            }
        """.trimIndent()
        val request = Request("PUT", "/_plugins/_security/api/internalusers/$username")
        val optionsBuilder = RequestOptions.DEFAULT.toBuilder()
        optionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(optionsBuilder)
        request.setJsonEntity(body)
        executeRequest(request)
    }

    protected fun deleteUser(username: String) {
        val request = Request("DELETE", "/_plugins/_security/api/internalusers/$username")
        try {
            executeRequest(request)
        } catch (_: Exception) {
        }
    }

    @Throws(IOException::class)
    protected fun createRoleMapping(role: String, users: List<String>, backendRoles: List<String> = emptyList()) {
        val usersJson =
            if (users.isEmpty()) {
                "[]"
            } else {
                users.joinToString(prefix = "[\"", postfix = "\"]", separator = "\", \"")
            }
        val backendRolesJson =
            if (backendRoles.isEmpty()) {
                "[]"
            } else {
                backendRoles.joinToString(prefix = "[\"", postfix = "\"]", separator = "\", \"")
            }
        val body = """
            {
              "users": $usersJson,
              "backend_roles": $backendRolesJson,
              "hosts": []
            }
        """.trimIndent()
        val request = Request("PUT", "/_plugins/_security/api/rolesmapping/$role")
        val optionsBuilder = RequestOptions.DEFAULT.toBuilder()
        optionsBuilder.addHeader("Content-Type", "application/json")
        request.setOptions(optionsBuilder)
        request.setJsonEntity(body)
        executeRequest(request)
    }

    protected fun isResourceSharingFeatureEnabled(): Boolean {
        return try {
            val settings = getAllClusterSettings() ?: return false
            val persistent = settings.getAsJsonObject("persistent")
            val key = "plugins.security.experimental.resource_sharing.enabled"
            val persistentVal = persistent?.get(key)?.asString
            if (persistentVal != null) {
                persistentVal.toBoolean()
            } else {
                val defaults = settings.getAsJsonObject("defaults")
                defaults?.get(key)?.asString?.toBoolean() ?: false
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            // Return false if settings cannot be retrieved or parsed
            false
        }
    }

    @Throws(IOException::class)
    protected fun updateResourceSharingSettings(enabled: Boolean, protectedTypes: List<String>): JsonObject {
        val enabledStr = enabled.toString()
        val protectedJson =
            if (protectedTypes.isEmpty()) {
                "[]"
            } else {
                protectedTypes.joinToString(prefix = "[\"", postfix = "\"]", separator = "\", \"")
            }
        val request = Request("PUT", "/_cluster/settings")
        val body = """
            {
              "persistent": {
                "plugins.security.experimental.resource_sharing.enabled": "$enabledStr",
                "plugins.security.experimental.resource_sharing.protected_types": $protectedJson
              }
            }
        """.trimIndent()
        request.setJsonEntity(body)
        val options = RequestOptions.DEFAULT.toBuilder()
        options.addHeader("Content-Type", "application/json")
        request.setOptions(options)
        return executeRequest(request)
    }

    @Throws(IOException::class)
    protected open fun initReportSecurityUsersAndRoles() {
        createReportFullRole()
        createReportReadRole()
        createReportNoAccessRole()

        val fullPassword = generatePassword(reportFullUser)
        createUser(reportFullUser, fullPassword, backendRoles = emptyList())

        val readPassword = generatePassword(reportReadUser)
        createUser(reportReadUser, readPassword, backendRoles = emptyList())

        val noAccessPassword = generatePassword(reportNoAccessUser)
        createUser(reportNoAccessUser, noAccessPassword, backendRoles = emptyList())

        createRoleMapping(reportFullRole, listOf(reportFullUser))
        createRoleMapping(reportReadRole, listOf(reportReadUser))
        createRoleMapping(reportNoAccessRole, listOf(reportNoAccessUser))

        val host = getClusterHosts().first()
        val hosts = arrayOf(host)

        reportsFullClient = SecureRestClientBuilder(hosts, isHttps(), reportFullUser, fullPassword)
            .setSocketTimeout(60000)
            .build()

        reportsReadClient = SecureRestClientBuilder(hosts, isHttps(), reportReadUser, readPassword)
            .setSocketTimeout(60000)
            .build()

        reportsNoAccessClient = SecureRestClientBuilder(hosts, isHttps(), reportNoAccessUser, noAccessPassword)
            .setSocketTimeout(60000)
            .build()
    }

    @Throws(IOException::class)
    private fun createReportFullRole() {
        val body = """
            {
              "cluster_permissions": [
                "cluster:admin/opensearch/reports/*",
                "cluster:admin/opendistro/reports/*",
                "cluster:admin/settings/update"
              ],
              "index_permissions": [
                {
                  "index_patterns": [
                    "*"
                  ],
                  "allowed_actions": [
                    "indices:data/read*",
                    "indices:data/write*",
                    "indices:admin/mappings/fields/get*",
                    "indices:admin/resolve/index"
                  ],
                  "dls": "",
                  "fls": [],
                  "masked_fields": []
                }
              ]
            }
        """.trimIndent()
        val request = Request("PUT", "/_plugins/_security/api/roles/$reportFullRole")
        val options = RequestOptions.DEFAULT.toBuilder()
        options.addHeader("Content-Type", "application/json")
        request.setOptions(options)
        request.setJsonEntity(body)
        executeRequest(request)
    }

    @Throws(IOException::class)
    private fun createReportReadRole() {
        val body = """
            {
              "cluster_permissions": [
                "cluster:admin/opensearch/reports/definition/get",
                "cluster:admin/opensearch/reports/definitions/list",
                "cluster:admin/opendistro/reports/definition/get",
                "cluster:admin/opendistro/reports/definitions/list"
              ],
              "index_permissions": [
                {
                  "index_patterns": [
                    ".opendistro-reports-*",
                    ".opensearch-reports-*"
                  ],
                  "allowed_actions": [
                    "indices:data/read*",
                    "indices:admin/mappings/fields/get*",
                    "indices:admin/resolve/index"
                  ],
                  "dls": "",
                  "fls": [],
                  "masked_fields": []
                }
              ]
            }
        """.trimIndent()
        val request = Request("PUT", "/_plugins/_security/api/roles/$reportReadRole")
        val options = RequestOptions.DEFAULT.toBuilder()
        options.addHeader("Content-Type", "application/json")
        request.setOptions(options)
        request.setJsonEntity(body)
        executeRequest(request)
    }

    @Throws(IOException::class)
    private fun createReportNoAccessRole() {
        val body = """
            {
              "cluster_permissions": [],
              "index_permissions": []
            }
        """.trimIndent()
        val request = Request("PUT", "/_plugins/_security/api/roles/$reportNoAccessRole")
        val options = RequestOptions.DEFAULT.toBuilder()
        options.addHeader("Content-Type", "application/json")
        request.setOptions(options)
        request.setJsonEntity(body)
        executeRequest(request)
    }

    @Throws(IOException::class)
    protected open fun deleteReportSecurityUsers() {
        deleteUser(reportFullUser)
        deleteUser(reportReadUser)
        deleteUser(reportNoAccessUser)
    }

    @Throws(IOException::class)
    fun shareConfig(client: RestClient, payload: String?): JsonObject {
        val request = Request("PUT", shareConfigUri)
        val options = RequestOptions.DEFAULT.toBuilder()
        options.addHeader("Content-Type", "application/json")
        request.setOptions(options)
        request.setJsonEntity(payload)
        return executeRequest(client, request)
    }

    @Throws(IOException::class)
    fun patchSharingInfo(client: RestClient, payload: String?): JsonObject {
        val request = Request("PATCH", shareConfigUri)
        val options = RequestOptions.DEFAULT.toBuilder()
        options.addHeader("Content-Type", "application/json")
        request.setOptions(options)
        request.setJsonEntity(payload)
        return executeRequest(client, request)
    }

    fun shareWithUserPayload(resourceId: String?, resourceIndex: String?, accessLevel: String?, user: String?): String {
        return String.format(
            Locale.ROOT,
            """
            {
              "resource_id": "%s",
              "resource_type": "%s",
              "share_with": {
                "%s" : {
                    "users": ["%s"]
                }
              }
            }
            
            """.trimIndent(),
            resourceId,
            resourceIndex,
            accessLevel,
            user
        )
    }

    enum class Recipient(val value: String) {
        USERS("users"),
        ROLES("roles"),
        BACKEND_ROLES("backend_roles")
    }

    class PatchSharingInfoPayloadBuilder {
        private var configId: String? = null
        private var configType: String? = null

        // accessLevel -> recipientType -> principals
        private val share: MutableMap<String?, MutableMap<String?, MutableSet<String?>?>> =
            HashMap<String?, MutableMap<String?, MutableSet<String?>?>>()
        private val revoke: MutableMap<String?, MutableMap<String?, MutableSet<String?>?>> =
            HashMap<String?, MutableMap<String?, MutableSet<String?>?>>()

        fun configId(resourceId: String?): PatchSharingInfoPayloadBuilder {
            this.configId = resourceId
            return this
        }

        fun configType(resourceType: String?): PatchSharingInfoPayloadBuilder {
            this.configType = resourceType
            return this
        }

        fun share(recipients: MutableMap<Recipient?, MutableSet<String?>?>?, accessLevel: String?) {
            mergeInto(share, accessLevel, recipients)
        }

        fun revoke(recipients: MutableMap<Recipient?, MutableSet<String?>?>?, accessLevel: String?) {
            mergeInto(revoke, accessLevel, recipients)
        }

        /* -------------------------------- Build -------------------------------- */
        private fun buildJsonString(
            input: MutableMap<String?, MutableMap<String?, MutableSet<String?>?>>
        ): String {
            val pieces = input.map { (key, value) ->
                runCatching {
                    "\"$key\" : ${toRecipientsJson(value)}"
                }.getOrElse { e ->
                    throw IOException("Failed to serialize recipients for key=$key", e)
                }
            }

            return pieces.joinToString(",")
        }

        fun build(): String {
            val allShares = buildJsonString(share)
            val allRevokes = buildJsonString(revoke)
            return String.format(
                Locale.ROOT,
                """
                {
                  "resource_id": "%s",
                  "resource_type": "%s",
                  "add": {
                    %s
                  },
                  "revoke": {
                    %s
                  }
                }
                
                """.trimIndent(),
                configId,
                configType,
                allShares,
                allRevokes
            )
        }

        companion object {
            /* ------------------------------ Internals ------------------------------ */
            @Suppress("LoopWithTooManyJumpStatements")
            private fun mergeInto(
                target: MutableMap<String?, MutableMap<String?, MutableSet<String?>?>>,
                accessLevel: String?,
                incoming: MutableMap<Recipient?, MutableSet<String?>?>?
            ) {
                if (incoming == null || incoming.isEmpty()) return
                val existing = target.getOrPut(accessLevel) { HashMap<String?, MutableSet<String?>?>() }
                for (e in incoming.entries) {
                    val key = e.key ?: continue
                    val value = e.value
                    if (value == null || value.isEmpty()) continue
                    val recipientSet = existing.getOrPut(key.name) { HashSet<String?>() }
                    recipientSet?.addAll(value)
                }
            }

            @Throws(IOException::class)
            private fun toRecipientsJson(recipientsParam: MutableMap<String?, MutableSet<String?>?>?): String? {
                val recipients = recipientsParam ?: mutableMapOf()

                val builder = XContentFactory.jsonBuilder()
                builder.startObject()

                for (recipient in Recipient.entries) {
                    val key = recipient.name
                    if (recipients.containsKey(key)) {
                        writeArray(builder, key, recipients.get(key))
                    }
                }

                builder.endObject()
                return builder.toString()
            }

            @Throws(IOException::class)
            private fun writeArray(builder: XContentBuilder, field: String, values: MutableSet<String?>?) {
                builder.startArray(field)
                if (values != null) {
                    for (v in values) {
                        builder.value(v)
                    }
                }
                builder.endArray()
            }
        }
    }

    protected fun makeRequest(
        client: RestClient?,
        method: String?,
        endpoint: String?,
        httpEntity: HttpEntity?
    ): Response? {
        val request = Request(method, endpoint)
        if (httpEntity != null) {
            request.entity = httpEntity
        }
        return client?.performRequest(request)
    }

    protected fun isForbidden(e: Exception): Boolean {
        return when (e) {
            is ResponseException -> e.response.statusLine.statusCode == 403
            else -> false
        }
    }

    protected fun waitForSharingVisibility(
        method: String?,
        endpoint: String?,
        httpEntity: HttpEntity?,
        client: RestClient?
    ): Response? {
        return Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(200)).until({
            try {
                return@until makeRequest(client, method, endpoint, httpEntity)
            } catch (e: ResponseException) {
                // Treat 403 as eventual-consistency: keep waiting
                if (e.response.statusLine.statusCode == 403) {
                    return@until null
                }
                // Anything else is unexpected: fail fast
                throw e
            }
        }, Matchers.notNullValue())
    }

    protected fun waitForRevokeNonVisibility(
        method: String?,
        endpoint: String?,
        httpEntity: HttpEntity?,
        client: RestClient?
    ) {
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(200)).until({
            val isRevoked = try {
                // Still visible (200) -> keep waiting
                makeRequest(client, method, endpoint, httpEntity)
                false
            } catch (e: ResponseException) {
                // Access revoked (403) -> we're done
                e.response.statusLine.statusCode == 403
            }
            return@until isRevoked
        }, Matchers.`is`(true))
    }

    companion object {
        internal interface IProxy {
            val version: String?
            var sessionId: String?

            fun getExecutionData(reset: Boolean): ByteArray?
            fun dump(reset: Boolean)
            fun reset()
        }

        /*
        * We need to be able to dump the jacoco coverage before the cluster shuts down.
        * The new internal testing framework removed some gradle tasks we were listening to,
        * to choose a good time to do it. This will dump the executionData to file after each test.
        * TODO: This is also currently just overwriting integTest.exec with the updated execData without
        *   resetting after writing each time. This can be improved to either write an exec file per test
        *   or by letting jacoco append to the file.
        * */
        @JvmStatic
        @AfterClass
        fun dumpCoverage() {
            // jacoco.dir set in esplugin-coverage.gradle, if it doesn't exist we don't
            // want to collect coverage, so we can return early
            val jacocoBuildPath = System.getProperty("jacoco.dir") ?: return
            val serverUrl = "service:jmx:rmi:///jndi/rmi://127.0.0.1:7777/jmxrmi"
            JMXConnectorFactory.connect(JMXServiceURL(serverUrl)).use { connector ->
                val proxy = MBeanServerInvocationHandler.newProxyInstance(
                    connector.mBeanServerConnection,
                    ObjectName("org.jacoco:type=Runtime"),
                    IProxy::class.java,
                    false
                )
                proxy.getExecutionData(false)?.let {
                    val path = Paths.get("$jacocoBuildPath/integTest.exec")
                    Files.write(path, it)
                }
            }
        }
    }
}
