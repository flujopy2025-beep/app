package com.seoaudit.app.feature.gsc.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for GSC Search Analytics query.
 */
@Serializable
data class GscAnalyticsRequest(
    val startDate: String,
    val endDate: String,
    val dimensions: List<String> = emptyList(),
    val rowLimit: Int = 1000,
    val dimensionFilterGroups: List<DimensionFilterGroup> = emptyList()
)

@Serializable
data class DimensionFilterGroup(
    val filters: List<DimensionFilter>
)

@Serializable
data class DimensionFilter(
    val dimension: String,
    val operator: String = "equals",
    val expression: String
)

/**
 * Response from GSC Search Analytics query.
 */
@Serializable
data class GscAnalyticsResponse(
    val rows: List<GscAnalyticsRow> = emptyList(),
    val responseAggregationType: String? = null
)

@Serializable
data class GscAnalyticsRow(
    val keys: List<String> = emptyList(),
    val clicks: Double = 0.0,
    val impressions: Double = 0.0,
    val ctr: Double = 0.0,
    val position: Double = 0.0
)

/**
 * DTO for a single GSC site entry.
 */
@Serializable
data class GscSiteDto(
    val siteUrl: String,
    val permissionLevel: String
)

/**
 * Response from GSC Sites list endpoint.
 */
@Serializable
data class GscSitesListResponse(
    @SerialName("siteEntry")
    val siteEntry: List<GscSiteDto> = emptyList()
)
