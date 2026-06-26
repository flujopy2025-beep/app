package com.seoaudit.app.feature.ai.data

import com.seoaudit.app.core.domain.model.ServiceType
import com.seoaudit.app.core.domain.repository.AiRepository
import com.seoaudit.app.core.domain.repository.CredentialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiRepository] that delegates to [LlmApiService].
 * Retrieves API keys from [CredentialRepository] (Android Keystore)
 * and selects the appropriate LLM provider based on available keys.
 */
@Singleton
class AiRepositoryImpl @Inject constructor(
    private val llmApiService: LlmApiService,
    private val credentialRepository: CredentialRepository
) : AiRepository {

    override suspend fun analyzeHtml(
        html: String,
        metrics: Any?,
        context: Any?
    ): Result<Any> {
        val (apiKey, provider) = getConfiguredProvider()
            ?: return Result.failure(
                IllegalStateException("No LLM provider configured. Store an API key first.")
            )

        val prompt = buildAnalysisPrompt(html, metrics, context)
        return llmApiService.generateContent(prompt, apiKey, provider)
    }

    override suspend fun generateCodeFix(
        issue: Any,
        originalCode: String
    ): Result<Any> {
        val (apiKey, provider) = getConfiguredProvider()
            ?: return Result.failure(
                IllegalStateException("No LLM provider configured. Store an API key first.")
            )

        val prompt = buildCodeFixPrompt(issue, originalCode)
        return llmApiService.generateContent(prompt, apiKey, provider)
    }

    override fun streamResponse(prompt: String): Flow<String> {
        // Launches a coroutine internally to resolve the API key;
        // returns an empty flow if no provider is configured.
        return kotlinx.coroutines.flow.flow {
            val (apiKey, provider) = getConfiguredProvider() ?: return@flow
            llmApiService.streamContent(prompt, apiKey, provider)
                .collect { chunk -> emit(chunk) }
        }
    }

    override suspend fun isConfigured(): Boolean {
        return getConfiguredProvider() != null
    }

    /**
     * Returns the first configured provider (Gemini preferred, Claude fallback).
     */
    private suspend fun getConfiguredProvider(): ProviderConfig? {
        // Prefer Gemini if configured
        val geminiKey = credentialRepository
            .retrieveCredential(ServiceType.GEMINI.credentialKey)
            .getOrNull()
        if (!geminiKey.isNullOrBlank()) {
            return ProviderConfig(geminiKey, LlmProvider.GEMINI)
        }

        // Fallback to Claude
        val claudeKey = credentialRepository
            .retrieveCredential(ServiceType.CLAUDE.credentialKey)
            .getOrNull()
        if (!claudeKey.isNullOrBlank()) {
            return ProviderConfig(claudeKey, LlmProvider.CLAUDE)
        }

        return null
    }

    private fun buildAnalysisPrompt(
        html: String,
        metrics: Any?,
        context: Any?
    ): String {
        return buildString {
            appendLine("Analyze the following HTML for SEO issues.")
            if (metrics != null) {
                appendLine("Page metrics: $metrics")
            }
            if (context != null) {
                appendLine("Context: $context")
            }
            appendLine("---")
            appendLine(html)
        }
    }

    private fun buildCodeFixPrompt(issue: Any, originalCode: String): String {
        return buildString {
            appendLine("Generate a code fix for the following SEO issue:")
            appendLine("Issue: $issue")
            appendLine("Original code:")
            appendLine("---")
            appendLine(originalCode)
        }
    }

    private data class ProviderConfig(
        val apiKey: String,
        val provider: LlmProvider
    )
}
