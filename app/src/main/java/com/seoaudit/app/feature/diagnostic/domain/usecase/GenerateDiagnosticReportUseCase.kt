package com.seoaudit.app.feature.diagnostic.domain.usecase

import com.seoaudit.app.core.domain.model.AuditReport
import com.seoaudit.app.core.domain.model.DiagnosticProblem
import com.seoaudit.app.core.domain.model.DiagnosticReport
import com.seoaudit.app.core.domain.model.ImpactLevel
import com.seoaudit.app.core.domain.model.TechnicalCategory
import com.seoaudit.app.core.domain.repository.PageMetrics
import java.time.Instant
import javax.inject.Inject

/**
 * Generates a DiagnosticReport from audit results and GSC metrics.
 *
 * - Sorts problems by impact descending (HIGH → MEDIUM → LOW).
 * - Groups by category when there are more than 10 problems.
 * - Validates: Requirements 11.1, 11.2, 11.3, 11.4
 */
class GenerateDiagnosticReportUseCase @Inject constructor() {

    companion object {
        const val GROUPING_THRESHOLD = 10
    }

    /**
     * Generate a diagnostic report from audit results and metrics.
     *
     * @param auditReport The audit report with detected issues
     * @param metrics Optional GSC metrics for the page
     * @return A structured DiagnosticReport sorted by impact
     */
    operator fun invoke(
        auditReport: AuditReport,
        metrics: PageMetrics?
    ): DiagnosticReport {
        val problems = buildProblemsFromAudit(auditReport, metrics)
        val sorted = sortByImpact(problems)
        val final = if (sorted.size > GROUPING_THRESHOLD) {
            groupByCategory(sorted)
        } else {
            sorted
        }
        return DiagnosticReport(
            problems = final,
            generatedAt = Instant.now()
        )
    }

    /**
     * Sort problems by impact level descending.
     * HIGH first, then MEDIUM, then LOW.
     */
    fun sortByImpact(
        problems: List<DiagnosticProblem>
    ): List<DiagnosticProblem> {
        return problems.sortedBy { it.impact.ordinal }
    }

    /**
     * Group problems by category, maintaining impact order
     * within each group.
     */
    fun groupByCategory(
        problems: List<DiagnosticProblem>
    ): List<DiagnosticProblem> {
        return problems
            .groupBy { it.category }
            .toSortedMap()
            .flatMap { (_, group) ->
                group.sortedBy { it.impact.ordinal }
            }
    }

    private fun buildProblemsFromAudit(
        auditReport: AuditReport,
        metrics: PageMetrics?
    ): List<DiagnosticProblem> {
        return auditReport.issues.map { issue ->
            DiagnosticProblem(
                description = issue.description,
                origin = determineOrigin(issue, metrics),
                impact = mapSeverityToImpact(issue.severity),
                proposedSolution = issue.recommendation,
                category = mapCategory(issue.category)
            )
        }
    }

    private fun determineOrigin(
        issue: com.seoaudit.app.core.domain.model.AuditIssue,
        metrics: PageMetrics?
    ): com.seoaudit.app.core.domain.model.ProblemOrigin {
        // If metrics are available and the issue relates to
        // performance data, origin is GSC
        if (metrics != null && isMetricsDriven(issue)) {
            return com.seoaudit.app.core.domain.model.ProblemOrigin.GSC
        }
        return com.seoaudit.app.core.domain.model.ProblemOrigin.CODE
    }

    private fun isMetricsDriven(
        issue: com.seoaudit.app.core.domain.model.AuditIssue
    ): Boolean {
        return issue.category ==
            com.seoaudit.app.core.domain.model.AuditCategory.LOAD_PERFORMANCE
    }

    private fun mapSeverityToImpact(
        severity: com.seoaudit.app.core.domain.model.Severity
    ): ImpactLevel {
        return when (severity) {
            com.seoaudit.app.core.domain.model.Severity.CRITICAL ->
                ImpactLevel.HIGH
            com.seoaudit.app.core.domain.model.Severity.WARNING ->
                ImpactLevel.MEDIUM
            com.seoaudit.app.core.domain.model.Severity.INFO ->
                ImpactLevel.LOW
        }
    }

    private fun mapCategory(
        category: com.seoaudit.app.core.domain.model.AuditCategory
    ): TechnicalCategory {
        return when (category) {
            com.seoaudit.app.core.domain.model.AuditCategory.LOAD_PERFORMANCE ->
                TechnicalCategory.PERFORMANCE
            com.seoaudit.app.core.domain.model.AuditCategory.STRUCTURED_DATA ->
                TechnicalCategory.STRUCTURED_DATA
            com.seoaudit.app.core.domain.model.AuditCategory.TITLE_TAG,
            com.seoaudit.app.core.domain.model.AuditCategory.META_DESCRIPTION,
            com.seoaudit.app.core.domain.model.AuditCategory.HEADINGS,
            com.seoaudit.app.core.domain.model.AuditCategory.ALT_ATTRIBUTES,
            com.seoaudit.app.core.domain.model.AuditCategory.INTERNAL_LINKS ->
                TechnicalCategory.HTML_STRUCTURE
        }
    }
}
