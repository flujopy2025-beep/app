package com.seoaudit.app.feature.wordpress.domain.model

/**
 * Represents the publish status of a WordPress page.
 */
enum class PageStatus {
    PUBLISH,
    DRAFT,
    PENDING,
    PRIVATE;

    companion object {
        fun fromString(value: String): PageStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: DRAFT
    }
}

/**
 * Domain model representing a WordPress page.
 */
data class WordPressPage(
    val id: Long,
    val title: String,
    val content: String,
    val excerpt: String,
    val slug: String,
    val status: PageStatus,
    val url: String,
    val siteUrl: String,
    val metaDescription: String
)

/**
 * Parameters for updating a WordPress page.
 */
data class PageUpdate(
    val title: String? = null,
    val content: String? = null,
    val excerpt: String? = null,
    val slug: String? = null,
    val status: PageStatus? = null
)

/**
 * Parameters for listing WordPress pages.
 */
data class PageListParams(
    val page: Int = 1,
    val perPage: Int = 20,
    val status: PageStatus? = null,
    val search: String? = null
)

/**
 * Paginated result wrapper for list queries.
 */
data class PaginatedResult<T>(
    val items: List<T>,
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int
)

/**
 * Domain model representing a WordPress theme.
 */
data class WpTheme(
    val stylesheet: String,
    val name: String,
    val version: String,
    val isActive: Boolean
)

/**
 * Domain model representing a WordPress plugin.
 */
data class WpPlugin(
    val plugin: String,
    val name: String,
    val version: String,
    val isActive: Boolean
)

/**
 * Result of a WordPress authentication attempt.
 */
data class WpAuthResult(
    val siteName: String,
    val siteUrl: String,
    val wpVersion: String,
    val hasWritePermission: Boolean
)

/**
 * Configuration for connecting to a WordPress site.
 */
data class WpConnectionConfig(
    val siteUrl: String,
    val username: String,
    val appPassword: String
)
