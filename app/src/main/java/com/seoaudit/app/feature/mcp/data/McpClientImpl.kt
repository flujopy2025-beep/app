package com.seoaudit.app.feature.mcp.data

import com.seoaudit.app.core.domain.model.McpClientCapabilities
import com.seoaudit.app.core.domain.model.McpClientInfo
import com.seoaudit.app.core.domain.model.McpResource
import com.seoaudit.app.core.domain.model.McpSession
import com.seoaudit.app.core.domain.model.McpSessionState
import com.seoaudit.app.core.domain.model.McpTool
import com.seoaudit.app.core.domain.model.McpToolResult
import com.seoaudit.app.core.domain.model.ResourceCapabilities
import com.seoaudit.app.core.domain.model.ToolCapabilities
import com.seoaudit.app.core.domain.repository.McpRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [McpRepository] that communicates with MCP servers
 * using JSON-RPC 2.0 over HTTP.
 *
 * Manages session state as a StateFlow and supports reconnection logic.
 *
 * Validates: Requirements 19.1, 19.2, 19.4
 */
@Singleton
class McpClientImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json
) : McpRepository {

    private val _sessionState =
        MutableStateFlow<McpSessionState>(McpSessionState.Disconnected)

    private val requestId = AtomicInteger(0)

    companion object {
        private const val PROTOCOL_VERSION = "2024-11-05"
        private const val CLIENT_NAME = "SEOAuditAndroid"
        private const val CLIENT_VERSION = "1.0.0"
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }

    override suspend fun connect(
        serverUrl: String
    ): Result<McpSession> = runCatching {
        _sessionState.value = McpSessionState.Connecting

        val initParams = InitializeParams(
            protocolVersion = PROTOCOL_VERSION,
            capabilities = McpClientCapabilities(
                tools = ToolCapabilities(listChanged = true),
                resources = ResourceCapabilities(subscribe = true)
            ),
            clientInfo = McpClientInfo(
                name = CLIENT_NAME,
                version = CLIENT_VERSION
            )
        )

        val response = sendRequest<InitializeResult>(
            serverUrl = serverUrl,
            sessionId = null,
            method = "initialize",
            params = json.encodeToJsonElement(initParams)
        )

        val sessionId = response.sessionId
            ?: generateSessionId()

        val session = McpSession(
            serverUrl = serverUrl,
            serverCapabilities = response.capabilities,
            sessionId = sessionId
        )

        _sessionState.value = McpSessionState.Connected(session)
        session
    }.onFailure { error ->
        _sessionState.value =
            McpSessionState.Error(error.message ?: "Connection failed")
    }

    override suspend fun callTool(
        session: McpSession,
        tool: String,
        params: JsonObject
    ): Result<McpToolResult> = runCatching {
        val toolCallParams = ToolCallParams(
            name = tool,
            arguments = params
        )

        val result = sendRequest<ToolCallResult>(
            serverUrl = session.serverUrl,
            sessionId = session.sessionId,
            method = "tools/call",
            params = json.encodeToJsonElement(toolCallParams)
        )

        if (result.isError) {
            val errorMessage = result.content
                .firstOrNull()?.text ?: "Tool call failed"
            McpToolResult.error(errorMessage)
        } else {
            val content = result.content
                .mapNotNull { it.text }
                .joinToString("\n")
            McpToolResult.success(content)
        }
    }

    override suspend fun listTools(
        session: McpSession
    ): Result<List<McpTool>> = runCatching {
        val result = sendRequest<ToolListResult>(
            serverUrl = session.serverUrl,
            sessionId = session.sessionId,
            method = "tools/list",
            params = null
        )
        result.tools
    }

    override suspend fun listResources(
        session: McpSession
    ): Result<List<McpResource>> = runCatching {
        val result = sendRequest<ResourceListResult>(
            serverUrl = session.serverUrl,
            sessionId = session.sessionId,
            method = "resources/list",
            params = null
        )
        result.resources
    }

    override fun observeSessionState(): Flow<McpSessionState> =
        _sessionState.asStateFlow()

    /**
     * Attempts to reconnect to the MCP server with exponential backoff.
     */
    suspend fun reconnect(serverUrl: String): Result<McpSession> {
        var lastError: Throwable? = null
        for (attempt in 1..MAX_RECONNECT_ATTEMPTS) {
            val result = connect(serverUrl)
            if (result.isSuccess) return result
            lastError = result.exceptionOrNull()
            val delayMs = (1L shl attempt) * 1000L // 2s, 4s, 8s
            kotlinx.coroutines.delay(delayMs)
        }
        return Result.failure(
            lastError ?: Exception("Reconnection failed")
        )
    }

    /**
     * Sends a JSON-RPC 2.0 request and deserializes the result.
     */
    private suspend inline fun <reified T> sendRequest(
        serverUrl: String,
        sessionId: String?,
        method: String,
        params: kotlinx.serialization.json.JsonElement?
    ): T {
        val request = JsonRpcRequest(
            jsonrpc = "2.0",
            id = requestId.incrementAndGet(),
            method = method,
            params = params
        )

        val responseText = httpClient.post("$serverUrl/mcp") {
            sessionId?.let { header("X-Session-Id", it) }
            setBody(json.encodeToString(
                JsonRpcRequest.serializer(), request
            ))
        }.body<String>()

        val rpcResponse = json.decodeFromString(
            JsonRpcResponse.serializer(), responseText
        )

        if (rpcResponse.error != null) {
            throw McpProtocolException(
                code = rpcResponse.error.code,
                message = rpcResponse.error.message
            )
        }

        val resultElement = rpcResponse.result
            ?: throw McpProtocolException(
                code = -32600,
                message = "Missing result in response"
            )

        return json.decodeFromJsonElement(resultElement)
    }

    private fun generateSessionId(): String =
        java.util.UUID.randomUUID().toString()
}

/**
 * Exception representing an MCP protocol error.
 */
class McpProtocolException(
    val code: Int,
    override val message: String
) : Exception("MCP Error ($code): $message")
