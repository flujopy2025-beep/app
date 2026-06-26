package com.seoaudit.app.feature.ai.data

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enum representing supported LLM providers.
 */
enum class LlmProvider {
    GEMINI,
    CLAUDE
}

/**
 * Ktor-based service for communicating with LLM APIs.
 * Supports both Gemini (Google) and Claude (Anthropic) providers.
 * Implements streaming via SSE and exponential backoff retry logic.
 */
@Singleton
class LlmApiService @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json
) {

    companion object {
        private const val GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro"
        private const val CLAUDE_BASE_URL =
            "https://api.anthropic.com/v1/messages"

        private const val MAX_RETRIES = 3
        private const val RETRY_BASE_DELAY_MS = 2000L
        private const val ANTHROPIC_VERSION = "2023-06-01"
    }

    /**
     * Sends a prompt to the specified LLM provider and returns
     * the full response text.
     *
     * Implements exponential backoff: 2s, 4s, 8s.
     */
    suspend fun generateContent(
        prompt: String,
        apiKey: String,
        provider: LlmProvider
    ): Result<String> {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                val response = when (provider) {
                    LlmProvider.GEMINI -> callGemini(prompt, apiKey)
                    LlmProvider.CLAUDE -> callClaude(prompt, apiKey)
                }
                if (response.status.isSuccess()) {
                    return Result.success(response.bodyAsText())
                }
                lastException = Exception(
                    "HTTP ${response.status.value}: ${response.bodyAsText()}"
                )
            } catch (e: Exception) {
                lastException = e
            }

            if (attempt < MAX_RETRIES - 1) {
                val delayMs = RETRY_BASE_DELAY_MS * (1L shl attempt)
                delay(delayMs)
            }
        }

        return Result.failure(
            lastException ?: Exception("Unknown error after $MAX_RETRIES retries")
        )
    }

    /**
     * Streams LLM response tokens via SSE as a Flow.
     * Emits each text chunk as it arrives from the server.
     */
    fun streamContent(
        prompt: String,
        apiKey: String,
        provider: LlmProvider
    ): Flow<String> = flow {
        val statement = when (provider) {
            LlmProvider.GEMINI -> prepareGeminiStream(prompt, apiKey)
            LlmProvider.CLAUDE -> prepareClaudeStream(prompt, apiKey)
        }

        statement.execute { response ->
            val channel = response.content
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data.isNotEmpty() && data != "[DONE]") {
                        emit(data)
                    }
                }
            }
        }
    }

    private suspend fun callGemini(
        prompt: String,
        apiKey: String
    ): HttpResponse {
        val body = buildGeminiRequestBody(prompt)
        return httpClient.post("$GEMINI_BASE_URL:generateContent") {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }
    }

    private suspend fun callClaude(
        prompt: String,
        apiKey: String
    ): HttpResponse {
        val body = buildClaudeRequestBody(prompt)
        return httpClient.post(CLAUDE_BASE_URL) {
            header("x-api-key", apiKey)
            header("anthropic-version", ANTHROPIC_VERSION)
            contentType(ContentType.Application.Json)
            setBody(body.toString())
        }
    }

    private suspend fun prepareGeminiStream(
        prompt: String,
        apiKey: String
    ) = httpClient.preparePost("$GEMINI_BASE_URL:streamGenerateContent") {
        parameter("key", apiKey)
        parameter("alt", "sse")
        contentType(ContentType.Application.Json)
        setBody(buildGeminiRequestBody(prompt).toString())
    }

    private suspend fun prepareClaudeStream(
        prompt: String,
        apiKey: String
    ) = httpClient.preparePost(CLAUDE_BASE_URL) {
        header("x-api-key", apiKey)
        header("anthropic-version", ANTHROPIC_VERSION)
        contentType(ContentType.Application.Json)
        setBody(buildClaudeStreamRequestBody(prompt).toString())
    }

    private fun buildGeminiRequestBody(prompt: String): JsonObject {
        return buildJsonObject {
            putJsonArray("contents") {
                add(buildJsonObject {
                    putJsonArray("parts") {
                        add(buildJsonObject { put("text", prompt) })
                    }
                })
            }
        }
    }

    private fun buildClaudeRequestBody(prompt: String): JsonObject {
        return buildJsonObject {
            put("model", "claude-3-haiku-20240307")
            put("max_tokens", 4096)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", prompt)
                })
            }
        }
    }

    private fun buildClaudeStreamRequestBody(prompt: String): JsonObject {
        return buildJsonObject {
            put("model", "claude-3-haiku-20240307")
            put("max_tokens", 4096)
            put("stream", true)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", prompt)
                })
            }
        }
    }
}
