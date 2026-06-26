package com.seoaudit.app.feature.gsc.domain.usecase

import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.PageMetrics
import com.seoaudit.app.core.domain.util.DateRange
import com.seoaudit.app.core.domain.util.DateUtils
import javax.inject.Inject

/**
 * Use case for retrieving metrics of a specific page from GSC.
 * Returns page-level metrics plus associated search queries.
 *
 * Validates: Requirements 2.2, 2.3
 */
class GetPageMetricsUseCase @Inject constructor(
    private val gscRepository: GscRepository
) {
    /**
     * Retrieves metrics for a specific page URL including top queries.
     *
     * @param siteUrl The GSC site URL
     * @param pageUrl The specific page URL to filter by
     * @param dateRange Optional date range; defaults to last 28 days
     * @return Result with PageMetrics or error
     */
    suspend operator fun invoke(
        siteUrl: String,
        pageUrl: String,
        dateRange: DateRange? = null
    ): Result<PageMetrics> {
        val range = dateRange ?: DateUtils.defaultDateRange()
        return gscRepository.getPageMetrics(siteUrl, pageUrl, range)
    }
}
