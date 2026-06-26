package com.seoaudit.app.core.domain.model

import com.seoaudit.app.core.domain.repository.PageMetrics
import java.time.Instant

/**
 * Severity levels for audit issues.
 */
enum class Severity { CRITICAL, WARNING, INFO }

/**
 * Categories of SEO audit checks.
 */
enum class AuditCategory {
    TITLE_TAG,
    META_DESCRIPTION,
    HEADINGS,
    ALT_ATTRIBUTES,
    INTERNAL_LINKS,
    STRUCTURED_DATA,
    LOAD_PERFORMANCE
}

/**
 * Represents a single SEO issue detected during audit.
 */
data class AuditIssue(
    val id: String,
    val category: AuditCategory,
    val severity: Severity,
    val title: String,
    val description: String,
    val impact: String,
    val recommendation: String,
    val element: String? = null,
    val fixable: Boolean
)

/**
 * Audit report for a single page.
 */
data class AuditReport(
    val pageUrl: String,
    val pageTitle: String,
    val score: Int,
    val issues: List<AuditIssue>,
    val metrics: PageMetrics?,
    val auditedAt: Instant
)

/**
 * Summary of audit findings across a site.
 */
data class AuditSummary(
    val critical: Int,
    val warnings: Int,
    val info: Int,
    val fixable: Int
)

/**
 * Consolidated report for a full site audit.
 */
data class SiteAuditReport(
    val siteUrl: String,
    val totalPages: Int,
    val auditedPages: Int,
    val overallScore: Int,
    val summary: AuditSummary,
    val pages: List<AuditReport>,
    val generatedAt: Instant
)

/**
 * Progress state emitted during a site-wide audit.
 */
sealed interface SiteAuditProgress {
    data class InProgress(
        val current: Int,
        val total: Int
    ) : SiteAuditProgress

    data class Complete(
        val report: SiteAuditReport
    ) : SiteAuditProgress

    data class Error(
        val error: Throwable
    ) : SiteAuditProgress
}
