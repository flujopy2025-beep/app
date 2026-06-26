package com.seoaudit.app.feature.codegen.domain.usecase

import com.seoaudit.app.core.domain.model.AuditIssue
import com.seoaudit.app.core.domain.model.CodeFix
import com.seoaudit.app.core.domain.model.DiffResult
import com.seoaudit.app.core.domain.repository.AiRepository
import com.seoaudit.app.feature.codegen.domain.engine.DiffEngine
import com.seoaudit.app.feature.codegen.domain.validator.HtmlValidator
import com.seoaudit.app.feature.codegen.domain.validator.SecurityValidator

/**
 * Use case that generates a validated code fix for an audit issue.
 * Invokes the LLM, validates the result for HTML and security issues,
 * and retries up to MAX_RETRIES times if validation fails.
 */
class GenerateCodeFixUseCase(
    private val aiRepository: AiRepository,
    private val diffEngine: DiffEngine,
    private val htmlValidator: HtmlValidator,
    private val securityValidator: SecurityValidator
) {
    companion object {
        const val MAX_RETRIES = 3
    }

    /**
     * Generates a code fix for the given audit issue.
     *
     * @param issue The SEO audit issue to fix
     * @param originalCode The original source code to fix
     * @return Result containing a CodeFix with validity status
     */
    suspend operator fun invoke(
        issue: AuditIssue,
        originalCode: String
    ): Result<CodeFix> {
        var lastErrors = emptyList<String>()

        repeat(MAX_RETRIES) { attempt ->
            val result = aiRepository.generateCodeFix(issue, originalCode)

            val proposedCode = result.getOrElse {
                return Result.failure(it)
            }

            val codeString = proposedCode.toString()
            val validationErrors = validateCode(codeString)

            val diff = diffEngine.generateDiff(originalCode, codeString)

            if (validationErrors.isEmpty()) {
                return Result.success(
                    buildCodeFix(
                        issue = issue,
                        originalCode = originalCode,
                        proposedCode = codeString,
                        diff = diff,
                        isValid = true,
                        errors = emptyList()
                    )
                )
            }
            lastErrors = validationErrors
        }

        // All retries exhausted, return with validation errors
        val finalDiff = diffEngine.generateDiff(originalCode, originalCode)
        return Result.success(
            buildCodeFix(
                issue = issue,
                originalCode = originalCode,
                proposedCode = originalCode,
                diff = finalDiff,
                isValid = false,
                errors = lastErrors
            )
        )
    }

    private fun validateCode(code: String): List<String> {
        val errors = mutableListOf<String>()
        val htmlErrors = htmlValidator.validate(code)
        htmlErrors.forEach { errors.add("HTML: ${it.message} (line ${it.line})") }
        val securityIssues = securityValidator.validate(code)
        securityIssues.forEach { errors.add("Security: ${it.message} (line ${it.line})") }
        return errors
    }

    private fun buildCodeFix(
        issue: AuditIssue,
        originalCode: String,
        proposedCode: String,
        diff: DiffResult,
        isValid: Boolean,
        errors: List<String>
    ): CodeFix {
        return CodeFix(
            issueId = issue.id,
            originalCode = originalCode,
            proposedCode = proposedCode,
            diff = diff,
            explanation = issue.recommendation,
            isValid = isValid,
            validationErrors = errors
        )
    }
}
