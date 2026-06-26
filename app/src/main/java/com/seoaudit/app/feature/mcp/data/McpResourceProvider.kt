package com.seoaudit.app.feature.mcp.data

import com.seoaudit.app.core.domain.model.ConnectionStatus as GscConnectionStatus
import com.seoaudit.app.core.domain.repository.ConnectionStatus as WpConnectionStatus
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.WordPressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * State representing a single service connection.
 */
@Serializable
data class ConnectionInfo(
    val service: String,
    val status: String,
    val message: String? = null
)

/**
 * Full resource state for the connections resource.
 */
@Serializable
data class ConnectionsResourceState(
    val uri: String = RESOURCE_URI,
    val connections: List<ConnectionInfo>
) {
    companion object {
        const val RESOURCE_URI = "seo-audit://connections"
    }
}

/**
 * MCP Resource Provider that exposes the `seo-audit://connections`
 * resource, reporting the current status of GSC and WordPress
 * connections in JSON format.
 *
 * Validates: Requirements 7.5
 */
@Singleton
class McpResourceProvider @Inject constructor(
    private val gscRepository: GscRepository,
    private val wpRepository: WordPressRepository,
    private val json: Json
) {

    companion object {
        const val RESOURCE_URI = "seo-audit://connections"
        const val RESOURCE_NAME = "Active Connections"
        const val RESOURCE_DESCRIPTION =
            "Current status of GSC and WordPress connections"
        const val RESOURCE_MIME_TYPE = "application/json"
    }

    /**
     * Observes the connections resource state as a Flow.
     * Combines GSC and WordPress connection statuses into
     * a single JSON resource.
     */
    fun observeConnectionsResource(): Flow<ConnectionsResourceState> {
        return combine(
            gscRepository.observeConnectionStatus(),
            wpRepository.observeConnectionStatus()
        ) { gscStatus, wpStatus ->
            ConnectionsResourceState(
                connections = listOf(
                    mapGscStatus(gscStatus),
                    mapWpStatus(wpStatus)
                )
            )
        }
    }

    /**
     * Returns the current connections resource state as JSON.
     */
    fun getResourceContentJson(state: ConnectionsResourceState): String {
        return json.encodeToString(
            ConnectionsResourceState.serializer(),
            state
        )
    }

    private fun mapGscStatus(
        status: GscConnectionStatus
    ): ConnectionInfo {
        return when (status) {
            is GscConnectionStatus.Connected -> ConnectionInfo(
                service = "gsc",
                status = "connected"
            )
            is GscConnectionStatus.Disconnected -> ConnectionInfo(
                service = "gsc",
                status = "disconnected"
            )
            is GscConnectionStatus.Error -> ConnectionInfo(
                service = "gsc",
                status = "error",
                message = status.message
            )
        }
    }

    private fun mapWpStatus(
        status: WpConnectionStatus
    ): ConnectionInfo {
        return when (status) {
            WpConnectionStatus.CONNECTED -> ConnectionInfo(
                service = "wordpress",
                status = "connected"
            )
            WpConnectionStatus.DISCONNECTED -> ConnectionInfo(
                service = "wordpress",
                status = "disconnected"
            )
            WpConnectionStatus.CONNECTING -> ConnectionInfo(
                service = "wordpress",
                status = "connecting"
            )
            WpConnectionStatus.ERROR -> ConnectionInfo(
                service = "wordpress",
                status = "error"
            )
        }
    }
}
