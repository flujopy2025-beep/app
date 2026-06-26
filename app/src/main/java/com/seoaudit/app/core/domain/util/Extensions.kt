package com.seoaudit.app.core.domain.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Result extensions
fun <T> Result<T>.getOrDefault(default: T): T = getOrElse { default }

// Flow extensions
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.success(it) }
    .catch { emit(Result.failure(it)) }

// String extensions for SEO
fun String.truncateForSeo(maxLength: Int): String =
    if (length <= maxLength) this else "${take(maxLength - 3)}..."

fun String.isValidUrl(): Boolean =
    matches(Regex("^https?://[\\w.-]+(?:\\.[\\w.-]+)+[\\w\\-._~:/?#\\[\\]@!\$&'()*+,;=.]+$"))
