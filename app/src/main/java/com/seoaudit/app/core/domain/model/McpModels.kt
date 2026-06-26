package com.seoaudit.app.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents an active MCP session with a remote server.
 */
@Serializable
data class McpSession(
    val serverUrl: String,
    val serverCapabilities: McpServerCapabilities,
    val sessionId: String
)

/**
 * Represents the current state of an MCP session.
 */
sealed interface McpSessionState {
    data object Disconnected : McpSessionState
    data object Connecting : McpSessionState
    data class Connected(val session: McpSession) : McpSessionState
    data class Error(val message: String) : McpSessionState
}

/**
 * Represents an MCP tool exposed by the server.
 */
@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

/**
 * Represents an MCP resource exposed by the server.
 */
@Serializable
data class McpResource(
    val uri: String,
    val name: String,
    val description: String,
    @SerialName("mimeType")
    val mimeType: String
)

/**
 * Result of calling an MCP tool.
 */
sealed interface McpToolResult {
    @Serializable
    data class Success(val content: String) : McpToolResult

    @Serializable
    data class Error(val code: Int, val message: String) : McpToolResult

    companion object {
        fun success(content: String): McpToolResult = Success(content)
        fun error(message: String, code: Int = -1): McpToolResult =
            Error(code, message)
    }
}

/**
 * Capabilities advertised by the MCP server.
 */
@Serializable
data class McpServerCapabilities(
    val tools: ToolCapabilities? = null,
    val resources: ResourceCapabilities? = null,
    val prompts: PromptCapabilities? = null
)

/**
 * Capabilities advertised by the MCP client.
 */
@Serializable
data class McpClientCapabilities(
    val tools: ToolCapabilities? = null,
    val resources: ResourceCapabilities? = null
)

/**
 * Information about this MCP client.
 */
@Serializable
data class McpClientInfo(
    val name: String,
    val version: String
)

@Serializable
data class ToolCapabilities(
    val listChanged: Boolean = false
)

@Serializable
data class ResourceCapabilities(
    val subscribe: Boolean = false
)

@Serializable
data class PromptCapabilities(
    val listChanged: Boolean = false
)
