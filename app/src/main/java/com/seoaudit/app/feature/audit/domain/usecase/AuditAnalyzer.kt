package com.seoaudit.app.feature.audit.domain.usecase

import com.seoaudit.app.core.domain.model.AuditReport
import com.seoaudit.app.core.domain.repository.PageMetrics
import com.seoaudit.app.feature.wordpress.domain.model.WordPressPage

/**
 * Interface for analyzing a WordPress page and producing
 * an audit report. Implementations may use AI/LLM or
 * rule-based analysis.
 */
interface AuditAnalyzer {
    /**
     * Analyzes a page with optional GSC metrics context.
     *
     * @param page The WordPress page to analyze
     * @param metrics Optional page metrics from GSC
     * @return Result containing the generated AuditReport
     */
    suspend fun analyze(
        page: WordPressPage,
        metrics: PageMetrics?
    ): Result<AuditReport>
}
