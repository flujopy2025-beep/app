package com.seoaudit.app.feature.mcp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.seoaudit.app.feature.mcp.data.ConnectionInfo
import com.seoaudit.app.feature.mcp.data.McpServerImpl
import com.seoaudit.app.feature.mcp.data.McpToolDefinition

/**
 * Data class representing the MCP Console UI state.
 */
data class McpConsoleUiState(
    val isServerRunning: Boolean = false,
    val tools: List<McpToolDefinition> = emptyList(),
    val connections: List<ConnectionInfo> = emptyList()
)

/**
 * MCP Console screen entry point with only onNavigateBack.
 * Uses default tool definitions and local state for the server toggle.
 */
@Composable
fun McpConsoleScreen(
    onNavigateBack: () -> Unit
) {
    var isRunning by remember { mutableStateOf(false) }
    val tools = remember { McpServerImpl.buildToolDefinitions() }
    val connections = remember {
        listOf(
            ConnectionInfo("gsc", "disconnected"),
            ConnectionInfo("wordpress", "disconnected")
        )
    }

    McpConsoleContent(
        uiState = McpConsoleUiState(
            isServerRunning = isRunning,
            tools = tools,
            connections = connections
        ),
        onToggleServer = { isRunning = it },
        onNavigateBack = onNavigateBack
    )
}

/**
 * MCP Console screen showing MCP server status,
 * registered tools, and active connections.
 * Uses Material Design 3 cards for each section.
 *
 * Validates: Requirements 7.1, 7.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpConsoleContent(
    uiState: McpConsoleUiState,
    onToggleServer: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MCP Console") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Server Status Card
            item {
                ServerStatusCard(
                    isRunning = uiState.isServerRunning,
                    onToggle = onToggleServer
                )
            }

            // Registered Tools Card
            item {
                RegisteredToolsCard(tools = uiState.tools)
            }

            // Active Connections Card
            item {
                ConnectionsCard(connections = uiState.connections)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ServerStatusCard(
    isRunning: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRunning)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isRunning)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MCP Server",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isRunning) "Running" else "Stopped",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isRunning,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@Composable
private fun RegisteredToolsCard(tools: List<McpToolDefinition>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registered Tools (${tools.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (tools.isEmpty()) {
                Text(
                    text = "No tools registered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tools.forEach { tool ->
                    ToolItem(tool = tool)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ToolItem(tool: McpToolDefinition) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        Column {
            Text(
                text = tool.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ConnectionsCard(connections: List<ConnectionInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Connections",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (connections.isEmpty()) {
                Text(
                    text = "No connections configured",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                connections.forEach { connection ->
                    ConnectionItem(connection = connection)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ConnectionItem(connection: ConnectionInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = connection.service.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (connection.status) {
                    "connected" -> Icons.Default.CheckCircle
                    "error" -> Icons.Default.Close
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = when (connection.status) {
                    "connected" -> Color(0xFF4CAF50)
                    "error" -> MaterialTheme.colorScheme.error
                    "connecting" -> Color(0xFFFFC107)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = connection.status.replaceFirstChar {
                    it.uppercase()
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
