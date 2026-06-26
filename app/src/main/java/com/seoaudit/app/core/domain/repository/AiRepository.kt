package com.seoaudit.app.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI/LLM operations.
 * Provides methods to analyze HTML content, generate code fixes,
 * stream responses, and check configuration status.
 */
interface AiRepository {

    /**
     * Analyzes HTML content using the configured LLM provider.
     *
     * @param html The HTML content to analyze
     * @param metrics Optional page metrics for context
     * @param context Optional audit context information
     * @return Result containing the analysis response
     */
    suspend fun analyzeHtml(
        html: String,
        metrics: Any?,
        context: Any?
    ): Result<Any>

    /**
     * Generates a code fix for a given SEO issue.
     *
     * @param issue The audit issue to fix
     * @param originalCode The original source code
     * @return Result containing the proposed fix
     */
    suspend fun generateCodeFix(
        issue: Any,
        originalCode: String
    ): Result<Any>

    /**
     * Streams LLM response tokens via SSE as a Flow.
     *
     * @param prompt The prompt to send to the LLM
     * @return Flow emitting response chunks as they arrive
     */
    fun streamResponse(prompt: String): Flow<String>

    /**
     * Checks whether the AI repository is properly configured
     * (i.e., an API key is stored for at least one LLM provider).
     *
     * @return true if at least one LLM provider is configured
     */
    suspend fun isConfigured(): Boolean
}
