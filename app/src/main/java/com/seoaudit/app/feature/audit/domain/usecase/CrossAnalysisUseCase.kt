package com.seoaudit.app.feature.audit.domain.usecase

import com.seoaudit.app.core.domain.model.AuditCategory
import com.seoaudit.app.core.domain.model.AuditIssue
import com.seoaudit.app.core.domain.model.DiagnosticProblem
import com.seoaudit.app.core.domain.model.DiagnosticReport
import com.seoaudit.app.core.domain.model.ImpactLevel
import com.seoaudit.app.core.domain.model.ProblemOrigin
import com.seoaudit.app.core.domain.model.Severity
import com.seoaudit.app.core.domain.model.TechnicalCategory
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.PageMetrics
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.core.domain.util.DateUtils
import com.seoaudit.app.feature.audit.domain.engine.SeoAuditEngine
import java.time.Instant
import javax.inject.Inject

/**
 * Result of cross-analysis combining GSC metrics with source code audit.
 */
data class CrossAnalysisResult(
    val underperformingPages: List<PageAnalysis>,
    val diagnosticReport: DiagnosticReport
)

/**
 * Analysis for a single page combining metrics and code issues.
 */
data class PageAnalysis(
    val pageUrl: String,
    val metrics: PageMetrics?,
    val auditIssues: List<AuditIssue>,
    val correlations: List<IssueMetricCorrelation>
)

/**
 * Correlation between a code issue and its impact on GSC metrics.
 */
data class IssueMetricCorrelation(
    val issue: AuditIssue,
    val affectedMetric: String,
    val estimatedImpact: ImpactLevel
)

/**
 * Cross-analysis use case that correlates GSC performance metrics
 * with source code problems to identify root causes of SEO issues.
 *
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5
 */
class CrossAnalysisUseCase @Inject constructor(
    private val gscRepository: GscRepository,
    private val wpRepository: WordPressRepository,
    private val auditEngine: SeoAuditEngine,
    private val identifyWorstPagesUseCase: IdentifyWorstPagesUseCase
) {
    /**
     * Executes cross-analysis for a site, correlating GSC data with code.
     *
     * @param siteUrl The site URL to analyze
     * @return Result containing CrossAnalysisResult with page analyses
     *         and a diagnostic report
     */
    suspend operator fun invoke(
        siteUrl: String
    ): Result<CrossAnalysisResult> = runCatching {
        // 1. Identify worst performing pages from GSC
        val worstPages = identifyWorstPagesUseCase(siteUrl).getOrThrow()

        // 2. For each worst page, fetch content and run audit
        val analyses = worstPages.take(MAX_PAGES_TO_ANALYZE).map { page ->
            analyzePageCrossData(siteUrl, page)
        }

        // 3. Build diagnostic report from all analyses
        val diagnosticReport = buildDiagnosticReport(analyses)

        CrossAnalysisResult(
            underperformingPages = analyses,
            diagnosticReport = diagnosticReport
        )
    }

    private suspend fun analyzePageCrossData(
        siteUrl: String,
        page: UnderperformingPage
    ): PageAnalysis {
        // Fetch page content from WordPress
        val html = fetchPageContent(page.pageUrl)

        // Run rule-based audit on HTML
        val auditResult = auditEngine.audit(html)

        // Fetch detailed metrics for the page
        val dateRange = DateUtils.defaultDateRange()
        val metrics = gscRepository.getPageMetrics(
            siteUrl, page.pageUrl, dateRange
        ).getOrNull()

        // Correlate issues with metric impact
        val correlations = correlateIssuesWithMetrics(
            auditResult.issues, metrics, page
        )

        return PageAnalysis(
            pageUrl = page.pageUrl,
            metrics = metrics,
            auditIssues = auditResult.issues,
            correlations = correlations
        )
    }

    /**
     * Fetches HTML content for a page URL from WordPress.
     * Returns empty string if page cannot be retrieved.
     */
    private suspend fun fetchPageContent(pageUrl: String): String {
        // Try to find the page by listing pages and matching URL
        val pagesResult = wpRepository.listPages(
            com.seoaudit.app.feature.wordpress.domain.model.PageListParams(
                perPage = 50
            )
        )
        val pages = pagesResult.getOrNull()?.items ?: return ""
        val matchedPage = pages.firstOrNull { it.url == pageUrl }
        return matchedPage?.content ?: ""
    }

    /**
     * Correlates detected code issues with their likely impact
     * on GSC metrics (CTR, Position, Impressions).
     */
    internal fun correlateIssuesWithMetrics(
        issues: List<AuditIssue>,
        metrics: PageMetrics?,
        page: UnderperformingPage
    ): List<IssueMetricCorrelation> {
        return issues.map { issue ->
            val (metric, impact) = mapIssueToMetricImpact(issue)
            IssueMetricCorrelation(
                issue = issue,
                affectedMetric = metric,
                estimatedImpact = impact
            )
        }
    }

    /**
     * Maps an audit issue category to the GSC metric it most affects
     * and the estimated impact level.
     */
    private fun mapIssueToMetricImpact(
        issue: AuditIssue
    ): Pair<String, ImpactLevel> {
        return when (issue.category) {
            AuditCategory.TITLE_TAG -> METRIC_CTR to ImpactLevel.HIGH
            AuditCategory.META_DESCRIPTION -> METRIC_CTR to ImpactLevel.HIGH
            AuditCategory.HEADINGS -> METRIC_POSITION to ImpactLevel.MEDIUM
            AuditCategory.STRUCTURED_DATA -> METRIC_POSITION to ImpactLevel.MEDIUM
            AuditCategory.LOAD_PERFORMANCE -> METRIC_IMPRESSIONS to ImpactLevel.HIGH
            AuditCategory.ALT_ATTRIBUTES -> METRIC_POSITION to ImpactLevel.LOW
            AuditCategory.INTERNAL_LINKS -> METRIC_POSITION to ImpactLevel.LOW
        }
    }

    /**
     * Builds a DiagnosticReport from all page analyses,
     * sorted by impact (HIGH first).
     */
    private fun buildDiagnosticReport(
        analyses: List<PageAnalysis>
    ): DiagnosticReport {
        val allProblems = analyses.flatMap { analysis ->
            analysis.auditIssues.map { issue ->
                DiagnosticProblem(
                    description = issue.description,
                    origin = determineOrigin(analysis.metrics),
                    impact = mapSeverityToImpact(issue.severity),
                    proposedSolution = issue.recommendation,
                    category = mapAuditToTechnicalCategory(issue.category)
                )
            }
        }.sortedBy { it.impact.ordinal }

        return DiagnosticReport(
            problems = allProblems,
            generatedAt = Instant.now()
        )
    }

    /**
     * Determines origin based on whether GSC metrics are available.
     * If metrics exist, origin is GSC; otherwise it's CODE.
     */
    private fun determineOrigin(metrics: PageMetrics?): ProblemOrigin {
        return if (metrics != null) ProblemOrigin.GSC else ProblemOrigin.CODE
    }

    /**
     * Maps audit severity to diagnostic impact level.
     */
    private fun mapSeverityToImpact(severity: Severity): ImpactLevel {
        return when (severity) {
            Severity.CRITICAL -> ImpactLevel.HIGH
            Severity.WARNING -> ImpactLevel.MEDIUM
            Severity.INFO -> ImpactLevel.LOW
        }
    }

    /**
     * Maps audit category to technical category for grouping.
     */
    private fun mapAuditToTechnicalCategory(
        category: AuditCategory
    ): TechnicalCategory {
        return when (category) {
            AuditCategory.LOAD_PERFORMANCE -> TechnicalCategory.PERFORMANCE
            AuditCategory.STRUCTURED_DATA -> TechnicalCategory.STRUCTURED_DATA
            AuditCategory.HEADINGS -> TechnicalCategory.HTML_STRUCTURE
            AuditCategory.TITLE_TAG -> TechnicalCategory.HTML_STRUCTURE
            AuditCategory.META_DESCRIPTION -> TechnicalCategory.HTML_STRUCTURE
            AuditCategory.ALT_ATTRIBUTES -> TechnicalCategory.HTML_STRUCTURE
            AuditCategory.INTERNAL_LINKS -> TechnicalCategory.HTML_STRUCTURE
        }
    }

    companion object {
        private const val MAX_PAGES_TO_ANALYZE = 10
        private const val METRIC_CTR = "CTR"
        private const val METRIC_POSITION = "Position"
        private const val METRIC_IMPRESSIONS = "Impressions"
    }
}
