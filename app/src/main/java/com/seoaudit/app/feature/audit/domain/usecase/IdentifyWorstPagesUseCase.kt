package com.seoaudit.app.feature.audit.domain.usecase

import com.seoaudit.app.core.domain.repository.GscDimension
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.PerformanceRow
import com.seoaudit.app.core.domain.util.DateRange
import com.seoaudit.app.core.domain.util.DateUtils
import javax.inject.Inject

data class UnderperformingPage(
    val pageUrl: String,
    val clicks: Int,
    val impressions: Int,
    val ctr: Double,
    val position: Double,
    val reason: UnderperformanceReason
)

enum class UnderperformanceReason {
    LOW_CTR,
    HIGH_POSITION,
    LOW_IMPRESSIONS,
    COMBINED
}

class IdentifyWorstPagesUseCase @Inject constructor(
    private val gscRepository: GscRepository
) {
    suspend operator fun invoke(
        siteUrl: String,
        dateRange: DateRange? = null,
        maxResults: Int = 20
    ): Result<List<UnderperformingPage>> = runCatching {
        val range = dateRange ?: DateUtils.defaultDateRange()
        val data = gscRepository.getPerformanceData(
            siteUrl, range, listOf(GscDimension.PAGE)
        ).getOrThrow()

        if (data.rows.size < 2) return@runCatching emptyList()

        val avgCtr = data.rows.map { it.ctr }.average()
        val avgPosition = data.rows.map { it.position }.average()
        val avgImpressions = data.rows.map { it.impressions.toDouble() }.average()

        data.rows
            .filter { row ->
                row.ctr < avgCtr ||
                    row.position > avgPosition ||
                    row.impressions < avgImpressions * 0.3
            }
            .map { row ->
                val reason = determineReason(row, avgCtr, avgPosition, avgImpressions)
                UnderperformingPage(
                    pageUrl = row.keys.firstOrNull() ?: "",
                    clicks = row.clicks,
                    impressions = row.impressions,
                    ctr = row.ctr,
                    position = row.position,
                    reason = reason
                )
            }
            .sortedWith(
                compareBy<UnderperformingPage> { it.ctr }
                    .thenByDescending { it.position }
            )
            .take(maxResults)
    }

    private fun determineReason(
        row: PerformanceRow,
        avgCtr: Double,
        avgPosition: Double,
        avgImpressions: Double
    ): UnderperformanceReason {
        val lowCtr = row.ctr < avgCtr
        val highPos = row.position > avgPosition
        val lowImpressions = row.impressions < avgImpressions * 0.3
        return when {
            lowCtr && highPos -> UnderperformanceReason.COMBINED
            lowCtr -> UnderperformanceReason.LOW_CTR
            highPos -> UnderperformanceReason.HIGH_POSITION
            lowImpressions -> UnderperformanceReason.LOW_IMPRESSIONS
            else -> UnderperformanceReason.COMBINED
        }
    }
}
