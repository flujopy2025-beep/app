package com.seoaudit.app.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.seoaudit.app.core.data.db.entities.WpPageCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WpPageCacheDao {

    @Query("SELECT * FROM wp_pages_cache WHERE pageId = :pageId AND (cached_at + ttl_seconds * 1000) > :now")
    suspend fun getCachedPage(pageId: Long, now: Long = System.currentTimeMillis()): WpPageCacheEntity?

    @Query("SELECT * FROM wp_pages_cache WHERE site_url = :siteUrl ORDER BY title ASC")
    fun observePages(siteUrl: String): Flow<List<WpPageCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(page: WpPageCacheEntity)

    @Query("DELETE FROM wp_pages_cache WHERE site_url = :siteUrl")
    suspend fun clearCache(siteUrl: String)
}
