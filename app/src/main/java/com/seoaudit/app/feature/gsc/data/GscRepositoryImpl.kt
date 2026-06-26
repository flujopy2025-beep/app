package com.seoaudit.app.feature.gsc.data

import com.seoaudit.app.core.domain.model.ConnectionStatus
import com.seoaudit.app.core.domain.repository.GscDimension
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.PageMetrics
import com.seoaudit.app.core.domain.repository.PerformanceData
import com.seoaudit.app.core.domain.repository.PerformanceRow
import com.seoaudit.app.core.domain.repository.QueryMetric
import com.seoaudit.app.core.domain.util.DateRange
import com.seoaudit.app.feature.gsc.data.remote.DimensionFilter
import com.seoaudit.app.feature.gsc.data.remote.DimensionFilterGroup
import com.seoaudit.app.feature.gsc.data.remote.GscAnalyticsRequest
import com.seoaudit.app.feature.gsc.data.remote.GscApiService
import com.seoaudit.app.feature.gsc.domain.model.GscAuthResult
import com.seoaudit.app.feature.gsc.domain.model.GscSite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GscRepository.
 * Maps DTOs from the GSC API to domain models.
 */
@Singleton
class GscRepositoryImpl @Inject constructor(
    private val apiService: GscApiService,
    private val tokenManager: GscTokenManager
) : GscRepository {

    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)

    override suspend fun authenticate(): Result<GscAuthResult> = runCatching {
        val response = apiService.listSites()
        val sites = response.siteEntry.map { dto ->
            GscSite(
                siteUrl = dto.siteUrl,
                permissionLevel = dto.permissionLevel
            )
        }
        _connectionStatus.value = ConnectionStatus.Connected
        GscAuthResult(sites = sites, email = "")
    }.onFailure {
        _connectionStatus.value = ConnectionStatus.Error(
            it.message ?: "Authentication failed"
        )
    }

    override suspend fun listSites(): Result<List<GscSite>> = runCatching {
        val response = apiService.listSites()
        response.siteEntry.map { dto ->
            GscSite(
                siteUrl = dto.siteUrl,
                permissionLevel = dto.permissionLevel
            )
        }
    }

    override suspend fun getPerformanceData(
        siteUrl: String,
        dateRange: DateRange,
        dimensions: List<GscDimension>
    ): Result<PerformanceData> = runCatching {
        val (startStr, endStr) = dateRange.toFormattedStrings()
        val request = GscAnalyticsRequest(
            startDate = startStr,
            endDate = endStr,
            dimensions = dimensions.map { it.name.lowercase() }
        )
        val response = apiService.queryAnalytics(siteUrl, request)
        PerformanceData(
            siteUrl = siteUrl,
            rows = response.rows.map { row ->
                PerformanceRow(
                    keys = row.keys,
                    clicks = row.clicks.toInt(),
                    impressions = row.impressions.toInt(),
                    ctr = row.ctr,
                    position = row.position
                )
            },
            dateRange = dateRange
        )
    }

    override suspend fun getPageMetrics(
        siteUrl: String,
        pageUrl: String,
        dateRange: DateRange
    ): Result<PageMetrics> = runCatching {
        val (startStr, endStr) = dateRange.toFormattedStrings()
        // Get overall page metrics
        val pageRequest = GscAnalyticsRequest(
            startDate = startStr,
            endDate = endStr,
            dimensions = listOf("page"),
            dimensionFilterGroups = listOf(
                DimensionFilterGroup(
                    filters = listOf(
                        DimensionFilter(
                            dimension = "page",
                            expression = pageUrl
                        )
                    )
                )
            )
        )
        val pageResponse = apiService.queryAnalytics(siteUrl, pageRequest)
        val pageRow = pageResponse.rows.firstOrNull()

        // Get queries for this page
        val queryRequest = GscAnalyticsRequest(
            startDate = startStr,
            endDate = endStr,
            dimensions = listOf("query"),
            dimensionFilterGroups = listOf(
                DimensionFilterGroup(
                    filters = listOf(
                        DimensionFilter(
                            dimension = "page",
                            expression = pageUrl
                        )
                    )
                )
            )
        )
        val queryResponse = apiService.queryAnalytics(siteUrl, queryRequest)

        PageMetrics(
            pageUrl = pageUrl,
            clicks = pageRow?.clicks?.toInt() ?: 0,
            impressions = pageRow?.impressions?.toInt() ?: 0,
            ctr = pageRow?.ctr ?: 0.0,
            averagePosition = pageRow?.position ?: 0.0,
            topQueries = queryResponse.rows.map { row ->
                QueryMetric(
                    query = row.keys.firstOrNull() ?: "",
                    clicks = row.clicks.toInt(),
                    impressions = row.impressions.toInt(),
                    ctr = row.ctr,
                    position = row.position
                )
            },
            dateRange = dateRange
        )
    }

    override fun observeConnectionStatus(): Flow<ConnectionStatus> {
        return _connectionStatus.asStateFlow()
    }
}
