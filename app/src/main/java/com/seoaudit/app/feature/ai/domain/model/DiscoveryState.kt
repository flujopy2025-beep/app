package com.seoaudit.app.feature.ai.domain.model

/**
 * Steps in the interactive discovery flow.
 * Each step represents a question the system asks the user.
 */
enum class DiscoveryStep {
    ASK_URL,
    ASK_WORDPRESS,
    ASK_PROBLEM,
    ASK_FILES,
    COMPLETE
}

/**
 * Holds the data collected during the discovery flow.
 * Updated progressively as the user answers each question.
 */
data class DiscoveryData(
    val siteUrl: String = "",
    val isWordPress: Boolean? = null,
    val mainProblem: String = "",
    val priorityFiles: String = "",
    val currentStep: DiscoveryStep = DiscoveryStep.ASK_URL
)

/**
 * Represents a single message in the chat conversation.
 * Used for both system questions and user responses.
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
