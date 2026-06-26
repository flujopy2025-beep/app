package com.seoaudit.app.core.domain.error

/**
 * Exception thrown when a WordPress page update fails.
 * Preserves the original content for rollback purposes.
 */
class UpdateFailedException(
    val pageId: Long,
    val originalContent: String,
    cause: Throwable? = null
) : Exception(
    "Failed to update page $pageId. Original content preserved.",
    cause
)
