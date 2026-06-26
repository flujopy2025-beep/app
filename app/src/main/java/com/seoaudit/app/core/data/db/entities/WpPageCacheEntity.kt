package com.seoaudit.app.core.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wp_pages_cache")
data class WpPageCacheEntity(
    @PrimaryKey val pageId: Long,
    @ColumnInfo(name = "site_url") val siteUrl: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long,
    @ColumnInfo(name = "ttl_seconds") val ttlSeconds: Int = 3600
)
