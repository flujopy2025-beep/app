package com.seoaudit.app.feature.wordpress.data.cache

import com.seoaudit.app.core.data.db.dao.WpPageCacheDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WordPress page cache operations.
 * Provides methods to clear site-specific cache and all cached data.
 */
@Singleton
class WpCacheManager @Inject constructor(
    private val wpPageCacheDao: WpPageCacheDao
) {
    /**
     * Clears all cached pages for a specific WordPress site.
     * @param siteUrl The base URL of the WordPress site.
     */
    suspend fun clearSiteCache(siteUrl: String) {
        wpPageCacheDao.clearCache(siteUrl)
    }

    /**
     * Retrieves a cached page if still valid (within TTL).
     * @param pageId The ID of the page to retrieve.
     * @return The cached page entity, or null if expired/not found.
     */
    suspend fun getCachedPage(pageId: Long) =
        wpPageCacheDao.getCachedPage(pageId)
}
