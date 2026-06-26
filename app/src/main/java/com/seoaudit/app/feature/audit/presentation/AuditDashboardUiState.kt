package com.seoaudit.app.feature.audit.presentation

/**
 * UI state for the Audit Dashboard screen.
 * Represents the different states of performance metrics loading.
 */
sealed interface AuditDashboardUiState {
    /** Initial state before any site is selected. */
    data object Idle : AuditDashboardUiState

    /** Loading performance data from GSC. */
    data object Loading : AuditDashboardUiState

    /** Successfully loaded performance metrics. */
    data class Success(
        val siteUrl: String,
        val clicks: Int,
        val impressions: Int,
        val ctr: Double,
        val position: Double,
        val dateRangeLabel: String
    ) : AuditDashboardUiState

    /** Error state with user-friendly message. */
    data class Error(val message: String) : AuditDashboardUiState
}
