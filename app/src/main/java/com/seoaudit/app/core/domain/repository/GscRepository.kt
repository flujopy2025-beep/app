package com.seoaudit.app.core.domain.repository

import com.seoaudit.app.core.domain.model.ConnectionStatus
import com.seoaudit.app.core.domain.util.DateRange
import com.seoaudit.app.feature.gsc.domain.model.GscAuthResult
import com.seoaudit.app.feature.gsc.domain.model.GscSite
import kotlinx.coroutines.flow.Flow

/**
 * Domain dimensions for GSC performance queries.
 */
enum class GscDimension { QUERY, PAGE, COUNTRY, DEVICE, DATE }

/**
 * Performance data row from GSC API.
 */
data class PerformanceRow(
    val keys: List<String>,
    val clicks: Int,
    val impressions: Int,
    val ctr: Double,
    val position: Double
)

/**
 * Aggregated performance data for a site.
 */
data class PerformanceData(
    val siteUrl: String,
    val rows: List<PerformanceRow>,
    val dateRange: DateRange
)

/**
 * Metric for an individual search query.
 */
data class QueryMetric(
    val query: String,
    val clicks: Int,
    val impressions: Int,
    val ctr: Double,
    val position: Double
)

/**
 * Metrics for a specific page including top queries.
 */
data class PageMetrics(
    val pageUrl: String,
    val clicks: Int,
    val impressions: Int,
    val ctr: Double,
    val averagePosition: Double,
    val topQueries: List<QueryMetric>,
    val dateRange: DateRange
)

/**
 * Repository interface for Google Search Console operations.
 * Handles authentication via OAuth 2.0 and data retrieval.
 */
interface GscRepository {
    suspend fun authenticate(): Result<GscAuthResult>
    suspend fun listSites(): Result<List<GscSite>>
    suspend fun getPerformanceData(
        siteUrl: String,
        dateRange: DateRange,
        dimensions: List<GscDimension>
    ): Result<PerformanceData>
    suspend fun getPageMetrics(
        siteUrl: String,
        pageUrl: String,
        dateRange: DateRange
    ): Result<PageMetrics>
    fun observeConnectionStatus(): Flow<ConnectionStatus>
}
