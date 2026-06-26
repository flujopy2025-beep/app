package com.seoaudit.app.core.domain.error

import com.seoaudit.app.core.domain.model.ServiceType

/**
 * Base sealed exception class for all SEO Audit app errors.
 * Provides user-friendly messages and retry guidance.
 *
 * Validates: Requirements 2.4, 14.4
 */
sealed class SeoAuditException(
    override val message: String,
    val userMessage: String,
    val retryable: Boolean,
    override val cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when the GSC API returns HTTP 429 (quota exceeded).
 * Includes retry-after hint in seconds.
 *
 * Validates: Requirements 2.4
 */
class QuotaExceededException(
    val retryAfterSeconds: Int,
    val service: ServiceType = ServiceType.GSC
) : SeoAuditException(
    message = "Cuota excedida para ${service.name}",
    userMessage = "Se alcanzó el límite de solicitudes. " +
        "Intente nuevamente en ${retryAfterSeconds}s.",
    retryable = true
)

/**
 * Thrown when authentication fails (invalid/expired credentials).
 */
class AuthenticationException(
    override val message: String,
    val service: ServiceType,
    cause: Throwable? = null
) : SeoAuditException(
    message = message,
    userMessage = "Error de autenticación con ${service.name}. " +
        "Verifique sus credenciales.",
    retryable = false,
    cause = cause
)

/**
 * Thrown when a network connection cannot be established.
 */
class ConnectionException(
    val url: String,
    cause: Throwable? = null
) : SeoAuditException(
    message = "No se pudo conectar a $url",
    userMessage = "Error de conexión. " +
        "Verifique su conectividad a internet.",
    retryable = true,
    cause = cause
)
