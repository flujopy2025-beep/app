package com.seoaudit.app.feature.audit.domain.usecase

import com.seoaudit.app.core.domain.model.AuditReport
import com.seoaudit.app.core.domain.model.AuditSummary
import com.seoaudit.app.core.domain.model.Severity
import com.seoaudit.app.core.domain.model.SiteAuditProgress
import com.seoaudit.app.core.domain.model.SiteAuditReport
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.feature.wordpress.domain.model.PageListParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

/**
 * Use case to perform a full site audit by iterating
 * over all available pages and generating a consolidated
 * SiteAuditReport with summary statistics.
 *
 * Emits SiteAuditProgress events as a Flow to allow
 * the UI to display real-time progress.
 */
class AuditSiteUseCase @Inject constructor(
    private val wpRepository: WordPressRepository,
    private val auditPageUseCase: AuditPageUseCase
) {
    /**
     * Starts a full site audit processing up to [maxPages].
     *
     * @param maxPages Maximum number of pages to audit
     * @return Flow emitting progress, completion or error
     */
    operator fun invoke(
        maxPages: Int = 50
    ): Flow<SiteAuditProgress> = flow {
        val pagesResult = wpRepository.listPages(
            PageListParams(perPage = maxPages)
        )
        val pages = pagesResult.getOrElse {
            emit(SiteAuditProgress.Error(it))
            return@flow
        }

        val reports = mutableListOf<AuditReport>()
        pages.items.forEachIndexed { index, page ->
            emit(
                SiteAuditProgress.InProgress(
                    current = index + 1,
                    total = pages.items.size
                )
            )
            auditPageUseCase(page.id, includeMetrics = false)
                .onSuccess { reports.add(it) }
        }

        val siteReport = buildSiteReport(pages, reports)
        emit(SiteAuditProgress.Complete(siteReport))
    }

    private fun buildSiteReport(
        pages: com.seoaudit.app.feature.wordpress.domain.model.PaginatedResult<com.seoaudit.app.feature.wordpress.domain.model.WordPressPage>,
        reports: List<AuditReport>
    ): SiteAuditReport {
        val siteUrl = pages.items.firstOrNull()?.siteUrl ?: ""
        val overallScore = if (reports.isNotEmpty()) {
            reports.map { it.score }.average().toInt()
        } else 0

        return SiteAuditReport(
            siteUrl = siteUrl,
            totalPages = pages.items.size,
            auditedPages = reports.size,
            overallScore = overallScore,
            summary = buildSummary(reports),
            pages = reports,
            generatedAt = Instant.now()
        )
    }

    private fun buildSummary(
        reports: List<AuditReport>
    ): AuditSummary {
        return AuditSummary(
            critical = reports.sumOf { r ->
                r.issues.count { it.severity == Severity.CRITICAL }
            },
            warnings = reports.sumOf { r ->
                r.issues.count { it.severity == Severity.WARNING }
            },
            info = reports.sumOf { r ->
                r.issues.count { it.severity == Severity.INFO }
            },
            fixable = reports.sumOf { r ->
                r.issues.count { it.fixable }
            }
        )
    }
}
