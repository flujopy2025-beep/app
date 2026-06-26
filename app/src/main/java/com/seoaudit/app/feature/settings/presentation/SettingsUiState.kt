package com.seoaudit.app.feature.settings.presentation

/**
 * UI state for the Settings screen showing connected accounts.
 */
data class SettingsUiState(
    val isGscConnected: Boolean = false,
    val isWordPressConnected: Boolean = false,
    val wpSiteUrl: String = "",
    val isGeminiKeySet: Boolean = false,
    val isClaudeKeySet: Boolean = false,
    val isDisconnecting: Boolean = false,
    val showDisconnectDialog: DisconnectDialogTarget? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Represents which service the disconnect dialog is targeting.
 */
enum class DisconnectDialogTarget {
    GSC,
    WORDPRESS,
    ALL
}
