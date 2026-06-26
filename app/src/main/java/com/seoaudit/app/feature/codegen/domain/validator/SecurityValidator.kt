package com.seoaudit.app.feature.codegen.domain.validator

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates generated code for security vulnerabilities.
 * Detects SQL injection and XSS patterns using regex-based detection.
 */
@Singleton
class SecurityValidator @Inject constructor() {

    private val sqlInjectionPatterns = listOf(
        Regex("""\$[a-zA-Z_]\w*\s*\.\s*["']?\s*(SELECT|INSERT|UPDATE|DELETE|DROP)""", RegexOption.IGNORE_CASE),
        Regex("""["']\s*\+\s*\$[a-zA-Z_]\w*\s*\+\s*["'].*?(WHERE|AND|OR)""", RegexOption.IGNORE_CASE),
        Regex("""\$wpdb->query\s*\(\s*["']\s*\w+.*?\$""", RegexOption.IGNORE_CASE),
        Regex("""query\s*\(\s*["'].*?\$[a-zA-Z_]\w*"""),
        Regex("""execute\s*\(\s*["'].*?\$[a-zA-Z_]\w*"""),
        Regex("""mysql_query\s*\(""", RegexOption.IGNORE_CASE),
        Regex("""SELECT\s+.*\s+FROM\s+.*\$[a-zA-Z_]\w*""", RegexOption.IGNORE_CASE),
        Regex("""WHERE\s+\w+\s*=\s*['"]?\s*\$[a-zA-Z_]\w*""", RegexOption.IGNORE_CASE)
    )

    private val xssPatterns = listOf(
        Regex("""echo\s+\$[a-zA-Z_]\w*\s*;"""),
        Regex("""echo\s+\$_(GET|POST|REQUEST)\s*\["""),
        Regex("""print\s*\(\s*\$[a-zA-Z_]\w*\s*\)"""),
        Regex("""innerHTML\s*=\s*[^;]*\$[a-zA-Z_]\w*"""),
        Regex("""document\.write\s*\("""),
        Regex("""\.html\s*\(\s*[^)]*\$[a-zA-Z_]\w*"""),
        Regex("""<[^>]*\$[a-zA-Z_]\w*[^>]*>""")
    )

    /**
     * Validates the given code and returns a list of security issues.
     * Returns an empty list if no vulnerabilities are detected.
     */
    fun validate(code: String): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()
        issues.addAll(detectSqlInjection(code))
        issues.addAll(detectXss(code))
        return issues
    }

    private fun detectSqlInjection(code: String): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()
        val lines = code.lines()
        lines.forEachIndexed { lineIndex, line ->
            sqlInjectionPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(line)) {
                    issues.add(
                        SecurityIssue(
                            type = SecurityIssueType.SQL_INJECTION,
                            line = lineIndex + 1,
                            message = "Potential SQL injection: direct variable in query",
                            severity = IssueSeverity.CRITICAL
                        )
                    )
                }
            }
        }
        return issues.distinctBy { it.line }
    }

    private fun detectXss(code: String): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()
        val lines = code.lines()
        lines.forEachIndexed { lineIndex, line ->
            xssPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(line)) {
                    issues.add(
                        SecurityIssue(
                            type = SecurityIssueType.XSS,
                            line = lineIndex + 1,
                            message = "Potential XSS: unescaped output in HTML context",
                            severity = IssueSeverity.CRITICAL
                        )
                    )
                }
            }
        }
        return issues.distinctBy { it.line }
    }
}

/**
 * Types of security vulnerabilities detected.
 */
enum class SecurityIssueType {
    SQL_INJECTION,
    XSS
}

/**
 * Severity of a detected security issue.
 */
enum class IssueSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Represents a detected security vulnerability.
 */
data class SecurityIssue(
    val type: SecurityIssueType,
    val line: Int,
    val message: String,
    val severity: IssueSeverity
)
