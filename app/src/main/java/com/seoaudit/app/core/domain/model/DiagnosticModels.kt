package com.seoaudit.app.core.domain.model

import java.time.Instant

/**
 * Origin of a diagnostic problem.
 * GSC = detected from Google Search Console metrics.
 * CODE = detected from source code analysis.
 */
enum class ProblemOrigin { GSC, CODE }

/**
 * Impact level for diagnostic problems.
 * Ordered from highest to lowest severity.
 */
enum class ImpactLevel { HIGH, MEDIUM, LOW }

/**
 * Technical categories for grouping problems.
 */
enum class TechnicalCategory {
    PERFORMANCE,
    HTML_STRUCTURE,
    STRUCTURED_DATA,
    BLOCKING_RESOURCES
}

/**
 * A single diagnostic problem detected during analysis.
 *
 * @param description Human-readable description of the problem
 * @param origin Whether the problem was detected from GSC or Code
 * @param impact The impact level (HIGH, MEDIUM, LOW)
 * @param proposedSolution Proposed fix for the problem
 * @param category Technical category for grouping
 */
data class DiagnosticProblem(
    val description: String,
    val origin: ProblemOrigin,
    val impact: ImpactLevel,
    val proposedSolution: String,
    val category: TechnicalCategory
)

/**
 * Complete diagnostic report with sorted problems.
 *
 * @param problems List of problems sorted by impact descending
 * @param generatedAt Timestamp when the report was generated
 */
data class DiagnosticReport(
    val problems: List<DiagnosticProblem>,
    val generatedAt: Instant
)
