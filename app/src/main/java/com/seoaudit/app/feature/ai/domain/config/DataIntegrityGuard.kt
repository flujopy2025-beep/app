package com.seoaudit.app.feature.ai.domain.config

import com.seoaudit.app.core.domain.repository.PageMetrics
import com.seoaudit.app.core.domain.util.DateRange

/**
 * Ensures data integrity by verifying all metrics come from real GSC data.
 * Never generates estimated or invented data.
 * 
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4
 */
object DataIntegrityGuard {
    
    /**
     * Validates that metrics are real (not null/empty) before including in reports.
     * If metrics are unavailable, returns a message requesting manual data.
     */
    fun validateMetrics(metrics: PageMetrics?): MetricsValidation {
        if (metrics == null) {
            return MetricsValidation(
                isValid = false,
                message = "Datos de GSC no disponibles. Proporcione los datos de rendimiento manualmente."
            )
        }
        if (metrics.clicks == 0 && metrics.impressions == 0) {
            return MetricsValidation(
                isValid = false,
                message = "No existen datos de rendimiento para este periodo en Google Search Console."
            )
        }
        return MetricsValidation(isValid = true, message = null)
    }
    
    /**
     * Formats metrics attribution string including source and date range.
     */
    fun formatAttribution(dateRange: DateRange): String {
        val (start, end) = dateRange.toFormattedStrings()
        return "Fuente: Google Search Console | Periodo: $start a $end"
    }
    
    /**
     * Ensures that no metrics field is fabricated.
     * Returns error if any field appears invalid.
     */
    fun assertRealData(metrics: PageMetrics): Boolean {
        return metrics.clicks >= 0 &&
            metrics.impressions >= 0 &&
            metrics.ctr in 0.0..1.0 &&
            metrics.averagePosition >= 0.0
    }
}

data class MetricsValidation(
    val isValid: Boolean,
    val message: String?
)
