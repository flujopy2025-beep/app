package com.seoaudit.app.core.domain.model

import java.time.Duration
import java.time.Instant

data class AuthLockoutState(
    val service: ServiceType,
    val failedAttempts: Int,
    val lastAttemptAt: Instant?,
    val lockedUntil: Instant?
) {
    fun isLocked(now: Instant): Boolean = lockedUntil != null && now < lockedUntil

    fun remainingLockoutTime(now: Instant): Duration? {
        val until = lockedUntil ?: return null
        return if (now < until) Duration.between(now, until) else null
    }
}

enum class ServiceType(val credentialKey: String) {
    GSC("gsc_credentials"),
    WORDPRESS("wp_credentials"),
    GEMINI("gemini_api_key"),
    CLAUDE("claude_api_key")
}

sealed interface ConnectionStatus {
    data object Connected : ConnectionStatus
    data object Disconnected : ConnectionStatus
    data class Error(val message: String) : ConnectionStatus
}
