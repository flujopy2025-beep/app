package com.seoaudit.app.core.domain.repository

import com.seoaudit.app.feature.wordpress.domain.model.PageListParams
import com.seoaudit.app.feature.wordpress.domain.model.PageUpdate
import com.seoaudit.app.feature.wordpress.domain.model.PaginatedResult
import com.seoaudit.app.feature.wordpress.domain.model.WordPressPage
import com.seoaudit.app.feature.wordpress.domain.model.WpAuthResult
import com.seoaudit.app.feature.wordpress.domain.model.WpConnectionConfig
import com.seoaudit.app.feature.wordpress.domain.model.WpPlugin
import com.seoaudit.app.feature.wordpress.domain.model.WpTheme
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for WordPress operations.
 * Defines the contract for authentication, page management,
 * and site information retrieval.
 */
interface WordPressRepository {

    /**
     * Authenticates with a WordPress site and verifies write permissions.
     */
    suspend fun authenticate(config: WpConnectionConfig): Result<WpAuthResult>

    /**
     * Retrieves a single page by ID.
     */
    suspend fun getPage(pageId: Long): Result<WordPressPage>

    /**
     * Lists pages with pagination and optional filters.
     */
    suspend fun listPages(params: PageListParams): Result<PaginatedResult<WordPressPage>>

    /**
     * Updates a page with the given changes.
     */
    suspend fun updatePage(pageId: Long, update: PageUpdate): Result<WordPressPage>

    /**
     * Lists all installed themes.
     */
    suspend fun listThemes(): Result<List<WpTheme>>

    /**
     * Lists all installed plugins.
     */
    suspend fun listPlugins(): Result<List<WpPlugin>>

    /**
     * Observes connection status changes.
     */
    fun observeConnectionStatus(): Flow<ConnectionStatus>
}

/**
 * Represents the connection status to an external service.
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}
