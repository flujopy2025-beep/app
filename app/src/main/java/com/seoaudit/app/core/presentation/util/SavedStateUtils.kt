package com.seoaudit.app.core.presentation.util

import androidx.lifecycle.SavedStateHandle

/**
 * Extension functions for preserving ViewModel state across
 * navigation and process death (Requirement 18.4).
 */
inline fun <reified T> SavedStateHandle.getOrDefault(key: String, default: T): T {
    return get<T>(key) ?: default
}

fun SavedStateHandle.saveState(key: String, value: Any?) {
    set(key, value)
}
