package com.seoaudit.app.feature.audit.domain.usecase

import com.seoaudit.app.core.domain.model.AuditReport
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.PageMetrics
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.core.domain.util.DateUtils
import com.seoaudit.app.feature.audit.domain.engine.SeoAuditEngine
import java.time.Instant
import javax.inject.Inject

/**
 * Orchestrates a single-page SEO audit.
 * Gets WordPress page content, optionally fetches GSC metrics,
 * runs the SeoAuditEngine on the HTML, and returns an AuditReport.
 */
class AuditPageUseCase @Inject constructor(
    private val wpRepository: WordPressRepository,
    private val gscRepository: GscRepository,
    private val auditEngine: SeoAuditEngine
) {

    /**
     * Audits a WordPress page by ID.
     *
     * @param pageId The WordPress page ID to audit
     * @param includeMetrics Whether to fetch GSC metrics (optional)
     * @return Result containing the AuditReport
     */
    suspend operator fun invoke(
        pageId: Long,
        includeMetrics: Boolean = true
    ): Result<AuditReport> {
        // 1. Get the WordPress page content
        val page = wpRepository.getPage(pageId).getOrElse {
            return Result.failure(it)
        }

        // 2. Optionally fetch GSC metrics
        val metrics: PageMetrics? = if (includeMetrics) {
            fetchMetrics(page.siteUrl, page.url)
        } else {
            null
        }

        // 3. Run the rule-based audit engine on HTML
        val auditResult = auditEngine.audit(page.content)

        // 4. Build and return the AuditReport
        val report = AuditReport(
            pageUrl = page.url,
            pageTitle = page.title,
            score = auditResult.score,
            issues = auditResult.issues,
            metrics = metrics,
            auditedAt = Instant.now()
        )
        return Result.success(report)
    }

    private suspend fun fetchMetrics(
        siteUrl: String,
        pageUrl: String
    ): PageMetrics? {
        val dateRange = DateUtils.defaultDateRange()
        return gscRepository.getPageMetrics(
            siteUrl = siteUrl,
            pageUrl = pageUrl,
            dateRange = dateRange
        ).getOrNull()
    }
}
