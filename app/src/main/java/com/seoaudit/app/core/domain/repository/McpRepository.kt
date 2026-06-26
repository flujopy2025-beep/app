package com.seoaudit.app.core.domain.repository

import com.seoaudit.app.core.domain.model.McpResource
import com.seoaudit.app.core.domain.model.McpSession
import com.seoaudit.app.core.domain.model.McpSessionState
import com.seoaudit.app.core.domain.model.McpTool
import com.seoaudit.app.core.domain.model.McpToolResult
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

/**
 * Repository interface for MCP (Model Context Protocol) client operations.
 * Manages connections to MCP servers, tool calls, and resource discovery.
 *
 * Validates: Requirements 19.1, 19.2, 19.4
 */
interface McpRepository {

    /**
     * Connects to an MCP server at the given URL,
     * performing the Initialize handshake.
     *
     * @param serverUrl Base URL of the MCP server
     * @return Result containing the established McpSession
     */
    suspend fun connect(serverUrl: String): Result<McpSession>

    /**
     * Calls a tool on the connected MCP server.
     *
     * @param session Active MCP session
     * @param tool Name of the tool to call
     * @param params JSON parameters for the tool
     * @return Result containing the tool execution result
     */
    suspend fun callTool(
        session: McpSession,
        tool: String,
        params: JsonObject
    ): Result<McpToolResult>

    /**
     * Lists all available tools on the connected MCP server.
     *
     * @param session Active MCP session
     * @return Result containing the list of available tools
     */
    suspend fun listTools(session: McpSession): Result<List<McpTool>>

    /**
     * Lists all available resources on the connected MCP server.
     *
     * @param session Active MCP session
     * @return Result containing the list of available resources
     */
    suspend fun listResources(session: McpSession): Result<List<McpResource>>

    /**
     * Observes the current session state as a Flow.
     *
     * @return Flow emitting session state changes
     */
    fun observeSessionState(): Flow<McpSessionState>
}
