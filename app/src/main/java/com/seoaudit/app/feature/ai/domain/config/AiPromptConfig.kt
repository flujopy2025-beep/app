package com.seoaudit.app.feature.ai.domain.config

/**
 * Configuration for AI prompts ensuring professional, technical tone.
 * All prompts maintain a direct, performance-focused communication style.
 * 
 * Validates: Requirements 16.1, 16.2, 16.3
 */
object AiPromptConfig {
    
    const val SYSTEM_PROMPT = """You are a Senior Full-Stack Software Engineer and Technical SEO Specialist. 
Your communication style is:
- Technical, direct, and professional
- Focused on performance efficiency and measurable SEO impact
- Every recommendation includes a concise technical justification of the expected benefit
- Results are data-driven and reference specific metrics when available"""

    const val AUDIT_PROMPT_PREFIX = """Analyze the following HTML for SEO issues. 
For each issue found, provide:
1. Technical description of the problem
2. Measurable impact on SEO metrics (CTR, position, indexing)
3. Specific code fix with justification of the performance benefit
Focus exclusively on actionable, high-impact recommendations."""

    const val CODE_FIX_PROMPT_PREFIX = """Generate a code fix for the following SEO issue.
Requirements:
- Code must follow modern standards (PHP 8.0+, ES6+, semantic HTML5)
- Code must be secure (no SQLi, no XSS, proper input sanitization)
- Include a brief technical justification of why this fix improves SEO performance
- Preserve all existing functionality not related to the fix"""

    fun buildAuditPrompt(html: String, metricsContext: String? = null): String {
        return buildString {
            appendLine(AUDIT_PROMPT_PREFIX)
            if (metricsContext != null) {
                appendLine("\nCurrent GSC Metrics: $metricsContext")
            }
            appendLine("\n---HTML START---")
            appendLine(html.take(50000)) // Limit to avoid token overflow
            appendLine("---HTML END---")
        }
    }
    
    fun buildCodeFixPrompt(issue: String, originalCode: String): String {
        return buildString {
            appendLine(CODE_FIX_PROMPT_PREFIX)
            appendLine("\nIssue: $issue")
            appendLine("\nOriginal code:")
            appendLine("```")
            appendLine(originalCode.take(10000))
            appendLine("```")
        }
    }
}
