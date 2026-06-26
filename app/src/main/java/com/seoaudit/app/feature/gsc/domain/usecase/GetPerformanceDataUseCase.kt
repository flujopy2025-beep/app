package com.seoaudit.app.feature.gsc.domain.usecase

import com.seoaudit.app.core.domain.repository.GscDimension
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.PerformanceData
import com.seoaudit.app.core.domain.util.DateRange
import com.seoaudit.app.core.domain.util.DateUtils
import javax.inject.Inject

/**
 * Use case for retrieving performance data from Google Search Console.
 * Applies default date range (last 28 days) if none specified.
 *
 * Validates: Requirements 2.1, 2.3
 */
class GetPerformanceDataUseCase @Inject constructor(
    private val gscRepository: GscRepository
) {
    /**
     * Retrieves performance data (clicks, impressions, CTR, position)
     * for the given site and optional date range/dimensions.
     *
     * @param siteUrl The GSC site URL to query
     * @param dateRange Optional date range; defaults to last 28 days
     * @param dimensions Dimensions to group by; defaults to PAGE
     * @return Result with PerformanceData or error
     */
    suspend operator fun invoke(
        siteUrl: String,
        dateRange: DateRange? = null,
        dimensions: List<GscDimension> = listOf(GscDimension.PAGE)
    ): Result<PerformanceData> {
        val range = dateRange ?: DateUtils.defaultDateRange()
        return gscRepository.getPerformanceData(siteUrl, range, dimensions)
    }
}
