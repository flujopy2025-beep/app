package com.seoaudit.app.feature.audit.domain.engine

import com.seoaudit.app.core.domain.model.AuditCategory
import com.seoaudit.app.core.domain.model.AuditIssue
import com.seoaudit.app.core.domain.model.Severity
import java.util.UUID

/**
 * Rule-based SEO audit engine that analyzes HTML content
 * across 7 categories and produces scored issues.
 */
class SeoAuditEngine {

    /**
     * Runs a full audit on the provided HTML content.
     * Returns a list of detected issues and the computed score.
     */
    fun audit(html: String): AuditResult {
        val issues = mutableListOf<AuditIssue>()
        issues.addAll(checkTitle(html))
        issues.addAll(checkMetaDescription(html))
        issues.addAll(checkHeadings(html))
        issues.addAll(checkAltAttributes(html))
        issues.addAll(checkInternalLinks(html))
        issues.addAll(checkStructuredData(html))
        issues.addAll(checkLoadPerformance(html))
        val score = calculateScore(issues)
        return AuditResult(issues = issues, score = score)
    }

    data class AuditResult(
        val issues: List<AuditIssue>,
        val score: Int
    )

    // --- Title Tag Checks ---

    internal fun checkTitle(html: String): List<AuditIssue> {
        val issues = mutableListOf<AuditIssue>()
        val titleRegex = Regex("<title[^>]*>(.*?)</title>", RegexOption.IGNORE_CASE)
        val match = titleRegex.find(html)

        if (match == null) {
            issues.add(createIssue(
                category = AuditCategory.TITLE_TAG,
                severity = Severity.CRITICAL,
                title = "Missing title tag",
                description = "The page does not have a <title> tag.",
                impact = "Search engines cannot determine the page topic.",
                recommendation = "Add a <title> tag with 30-60 characters.",
                fixable = true
            ))
        } else {
            val titleText = match.groupValues[1].trim()
            if (titleText.isEmpty()) {
                issues.add(createIssue(
                    category = AuditCategory.TITLE_TAG,
                    severity = Severity.CRITICAL,
                    title = "Empty title tag",
                    description = "The <title> tag is present but empty.",
                    impact = "Search engines display empty title in results.",
                    recommendation = "Add descriptive text (30-60 chars).",
                    element = "<title></title>",
                    fixable = true
                ))
            } else if (titleText.length < 30) {
                issues.add(createIssue(
                    category = AuditCategory.TITLE_TAG,
                    severity = Severity.CRITICAL,
                    title = "Title too short (${titleText.length} chars)",
                    description = "Title has fewer than 30 characters.",
                    impact = "May not fully convey page relevance.",
                    recommendation = "Expand title to 30-60 characters.",
                    element = titleText,
                    fixable = true
                ))
            } else if (titleText.length > 60) {
                issues.add(createIssue(
                    category = AuditCategory.TITLE_TAG,
                    severity = Severity.CRITICAL,
                    title = "Title too long (${titleText.length} chars)",
                    description = "Title exceeds 60 characters.",
                    impact = "Gets truncated in search results.",
                    recommendation = "Shorten title to 30-60 characters.",
                    element = titleText,
                    fixable = true
                ))
            }
        }
        return issues
    }

    // --- Meta Description Checks ---

    internal fun checkMetaDescription(html: String): List<AuditIssue> {
        val issues = mutableListOf<AuditIssue>()
        val metaRegex = Regex(
            """<meta\s+[^>]*name\s*=\s*["']description["'][^>]*content\s*=\s*["'](.*?)["'][^>]*/?>""",
            RegexOption.IGNORE_CASE
        )
        val metaRegex2 = Regex(
            """<meta\s+[^>]*content\s*=\s*["'](.*?)["'][^>]*name\s*=\s*["']description["'][^>]*/?>""",
            RegexOption.IGNORE_CASE
        )
        val match = metaRegex.find(html) ?: metaRegex2.find(html)

        if (match == null) {
            issues.add(createIssue(
                category = AuditCategory.META_DESCRIPTION,
                severity = Severity.WARNING,
                title = "Missing meta description",
                description = "No meta description tag found.",
                impact = "Search engines may generate a suboptimal snippet.",
                recommendation = "Add a meta description (120-160 chars).",
                fixable = true
            ))
        } else {
            val desc = match.groupValues[1].trim()
            if (desc.isEmpty()) {
                issues.add(createIssue(
                    category = AuditCategory.META_DESCRIPTION,
                    severity = Severity.WARNING,
                    title = "Empty meta description",
                    description = "Meta description tag is empty.",
                    impact = "No custom snippet for search results.",
                    recommendation = "Write a compelling description (120-160 chars).",
                    fixable = true
                ))
            } else if (desc.length < 120) {
                issues.add(createIssue(
                    category = AuditCategory.META_DESCRIPTION,
                    severity = Severity.WARNING,
                    title = "Meta description too short (${desc.length} chars)",
                    description = "Description has fewer than 120 characters.",
                    impact = "Missed opportunity to attract clicks.",
                    recommendation = "Expand to 120-160 characters.",
                    element = desc,
                    fixable = true
                ))
            } else if (desc.length > 160) {
                issues.add(createIssue(
                    category = AuditCategory.META_DESCRIPTION,
                    severity = Severity.WARNING,
                    title = "Meta description too long (${desc.length} chars)",
                    description = "Description exceeds 160 characters.",
                    impact = "Gets truncated in search results.",
                    recommendation = "Shorten to 120-160 characters.",
                    element = desc,
                    fixable = true
                ))
            }
        }
        return issues
    }

    // --- Heading Checks ---

    internal fun checkHeadings(html: String): List<AuditIssue> {
        val issues = mutableListOf<AuditIssue>()
        val h1Regex = Regex("<h1[^>]*>", RegexOption.IGNORE_CASE)
        val h1Matches = h1Regex.findAll(html).toList()

        if (h1Matches.isEmpty()) {
            issues.add(createIssue(
                category = AuditCategory.HEADINGS,
                severity = Severity.CRITICAL,
                title = "Missing H1 heading",
                description = "No H1 tag found on the page.",
                impact = "Main topic signal missing for search engines.",
                recommendation = "Add exactly one H1 tag with the main keyword.",
                fixable = true
            ))
        } else if (h1Matches.size > 1) {
            issues.add(createIssue(
                category = AuditCategory.HEADINGS,
                severity = Severity.CRITICAL,
                title = "Multiple H1 headings (${h1Matches.size})",
                description = "Page has ${h1Matches.size} H1 tags.",
                impact = "Dilutes the main topic signal.",
                recommendation = "Keep only one H1 and demote others to H2.",
                fixable = true
            ))
        }

        val h2Regex = Regex("<h2[^>]*>", RegexOption.IGNORE_CASE)
        if (!h2Regex.containsMatchIn(html)) {
            issues.add(createIssue(
                category = AuditCategory.HEADINGS,
                severity = Severity.WARNING,
                title = "No H2 headings found",
                description = "Page lacks H2 subheadings.",
                impact = "Content structure is unclear to crawlers.",
                recommendation = "Add H2 headings to organize content sections.",
                fixable = true
            ))
        }
        return issues
    }

    // --- Alt Attributes Checks ---

    internal fun checkAltAttributes(html: String): List<AuditIssue> {
        val issues = mutableListOf<AuditIssue>()
        val imgRegex = Regex("<img\\s[^>]*>", RegexOption.IGNORE_CASE)
        val imgTags = imgRegex.findAll(html).toList()
        val missingAlt = imgTags.filter { img ->
            val tag = img.value
            !tag.contains(Regex("""alt\s*=\s*["'][^"']+["']""", RegexOption.IGNORE_CASE))
        }

        if (missingAlt.isNotEmpty()) {
            issues.add(createIssue(
                category = AuditCategory.ALT_ATTRIBUTES,
                severity = Severity.WARNING,
                title = "${missingAlt.size} image(s) without alt text",
                description = "Found ${missingAlt.size} <img> tags missing alt attribute.",
                impact = "Images won't be indexed; accessibility issue.",
                recommendation = "Add descriptive alt text to all images.",
                element = missingAlt.firstOrNull()?.value,
                fixable = true
            ))
        }
        return issues
    }

    // --- Internal Links Checks ---

    internal fun checkInternalLinks(html: String): List<AuditIssue> {
        val issues = mutableListOf<AuditIssue>()
        val linkRegex = Regex(
            """<a\s[^>]*href\s*=\s*["'](/[^"']*|(?!https?://|mailto:|tel:|#)[^"']*)["'][^>]*>""",
            RegexOption.IGNORE_CASE
        )
        val internalLinks = linkRegex.findAll(html).toList()

        if (internalLinks.isEmpty()) {
            issues.add(createIssue(
                category = AuditCategory.INTERNAL_LINKS,
                severity = Severity.WARNING,
                title = "No internal links found",
                description = "Page has no links to other pages on the same site.",
                impact = "Poor link equity distribution; crawlability reduced.",
                recommendation = "Add internal links to relevant content.",
                fixable = false
            ))
        }
        return issues
    }

    // --- Structured Data Checks ---

    internal fun checkStructuredData(html: String): List<AuditIssue> {
        val issues = mutableListOf<AuditIssue>()
        val jsonLdRegex = Regex(
            """<script[^>]*type\s*=\s*["']application/ld\+json["'][^>]*>""",
            RegexOption.IGNORE_CASE
        )
        val microdataRegex = Regex(
            """itemscope|itemtype\s*=\s*["']https?://schema\.org""",
            RegexOption.IGNORE_CASE
        )

        val hasJsonLd = jsonLdRegex.containsMatchIn(html)
        val hasMicrodata = microdataRegex.containsMatchIn(html)

        if (!hasJsonLd && !hasMicrodata) {
            issues.add(createIssue(
                category = AuditCategory.STRUCTURED_DATA,
                severity = Severity.INFO,
                title = "No structured data found",
                description = "No JSON-LD or Microdata markup detected.",
                impact = "Missing rich snippets in search results.",
                recommendation = "Add JSON-LD structured data (Schema.org).",
                fixable = true
            ))
        }
        return issues
    }

    // --- Load Performance Checks ---

    internal fun checkLoadPerformance(html: String): List<AuditIssue> {
        val issues = mutableListOf<AuditIssue>()
        val renderBlockingCss = Regex(
            """<link[^>]*rel\s*=\s*["']stylesheet["'][^>]*>""",
            RegexOption.IGNORE_CASE
        )
        val cssCount = renderBlockingCss.findAll(html).count()

        if (cssCount > 5) {
            issues.add(createIssue(
                category = AuditCategory.LOAD_PERFORMANCE,
                severity = Severity.WARNING,
                title = "Too many render-blocking stylesheets ($cssCount)",
                description = "Found $cssCount external CSS files blocking render.",
                impact = "Increases First Contentful Paint time.",
                recommendation = "Combine/inline critical CSS; defer non-critical.",
                fixable = false
            ))
        }

        val scriptRegex = Regex(
            """<script[^>]*src\s*=\s*["'][^"']+["'][^>]*>""",
            RegexOption.IGNORE_CASE
        )
        val nonAsyncScripts = scriptRegex.findAll(html).filter { match ->
            val tag = match.value
            !tag.contains("async", ignoreCase = true) &&
                !tag.contains("defer", ignoreCase = true)
        }.count()

        if (nonAsyncScripts > 3) {
            issues.add(createIssue(
                category = AuditCategory.LOAD_PERFORMANCE,
                severity = Severity.WARNING,
                title = "Render-blocking scripts ($nonAsyncScripts)",
                description = "$nonAsyncScripts scripts without async/defer.",
                impact = "Blocks page rendering until scripts load.",
                recommendation = "Add async or defer to non-critical scripts.",
                fixable = true
            ))
        }
        return issues
    }

    // --- Score Calculation ---

    /**
     * Calculates an overall score 0-100 based on weighted issues.
     * CRITICAL = -15, WARNING = -8, INFO = -3 per issue.
     */
    internal fun calculateScore(issues: List<AuditIssue>): Int {
        var score = 100
        for (issue in issues) {
            score -= when (issue.severity) {
                Severity.CRITICAL -> 15
                Severity.WARNING -> 8
                Severity.INFO -> 3
            }
        }
        return score.coerceIn(0, 100)
    }

    private fun createIssue(
        category: AuditCategory,
        severity: Severity,
        title: String,
        description: String,
        impact: String,
        recommendation: String,
        element: String? = null,
        fixable: Boolean
    ): AuditIssue = AuditIssue(
        id = UUID.randomUUID().toString(),
        category = category,
        severity = severity,
        title = title,
        description = description,
        impact = impact,
        recommendation = recommendation,
        element = element,
        fixable = fixable
    )
}
