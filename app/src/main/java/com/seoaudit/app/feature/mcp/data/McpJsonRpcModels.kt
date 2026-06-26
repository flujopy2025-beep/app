package com.seoaudit.app.feature.mcp.data

import com.seoaudit.app.core.domain.model.McpClientCapabilities
import com.seoaudit.app.core.domain.model.McpClientInfo
import com.seoaudit.app.core.domain.model.McpResource
import com.seoaudit.app.core.domain.model.McpServerCapabilities
import com.seoaudit.app.core.domain.model.McpTool
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * JSON-RPC 2.0 request envelope for MCP protocol messages.
 */
@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int,
    val method: String,
    val params: JsonElement? = null
)

/**
 * JSON-RPC 2.0 response envelope.
 */
@Serializable
data class JsonRpcResponse(
    val jsonrpc: String = "2.0",
    val id: Int? = null,
    val result: JsonElement? = null,
    val error: JsonRpcError? = null
)

/**
 * JSON-RPC 2.0 error object.
 */
@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

/**
 * Parameters for the MCP Initialize request.
 */
@Serializable
data class InitializeParams(
    val protocolVersion: String,
    val capabilities: McpClientCapabilities,
    val clientInfo: McpClientInfo
)

/**
 * Result of the MCP Initialize response.
 */
@Serializable
data class InitializeResult(
    val protocolVersion: String,
    val capabilities: McpServerCapabilities,
    val serverInfo: ServerInfo? = null,
    val sessionId: String? = null
)

@Serializable
data class ServerInfo(
    val name: String,
    val version: String? = null
)

/**
 * Parameters for the tools/call request.
 */
@Serializable
data class ToolCallParams(
    val name: String,
    val arguments: JsonObject? = null
)

/**
 * Result of the tools/list response.
 */
@Serializable
data class ToolListResult(
    val tools: List<McpTool> = emptyList()
)

/**
 * Result of the resources/list response.
 */
@Serializable
data class ResourceListResult(
    val resources: List<McpResource> = emptyList()
)

/**
 * Content item in a tool call result.
 */
@Serializable
data class ToolCallResultContent(
    val type: String = "text",
    val text: String? = null
)

/**
 * Result of the tools/call response.
 */
@Serializable
data class ToolCallResult(
    val content: List<ToolCallResultContent> = emptyList(),
    @SerialName("isError")
    val isError: Boolean = false
)
