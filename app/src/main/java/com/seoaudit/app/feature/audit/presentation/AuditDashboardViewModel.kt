package com.seoaudit.app.feature.audit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seoaudit.app.core.domain.error.QuotaExceededException
import com.seoaudit.app.feature.gsc.domain.usecase.GetPerformanceDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Audit Dashboard screen.
 * Loads GSC performance metrics for the selected site.
 *
 * Validates: Requirements 2.1, 2.4, 14.1, 14.3
 */
@HiltViewModel
class AuditDashboardViewModel @Inject constructor(
    private val getPerformanceDataUseCase: GetPerformanceDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuditDashboardUiState>(
        AuditDashboardUiState.Idle
    )
    val uiState: StateFlow<AuditDashboardUiState> =
        _uiState.asStateFlow()

    /**
     * Loads performance data for the given site URL.
     * Uses default 28-day date range.
     */
    fun loadPerformanceData(siteUrl: String) {
        viewModelScope.launch {
            _uiState.value = AuditDashboardUiState.Loading
            getPerformanceDataUseCase(siteUrl)
                .onSuccess { data ->
                    val totalClicks = data.rows.sumOf { it.clicks }
                    val totalImpressions = data.rows.sumOf { it.impressions }
                    val avgCtr = if (totalImpressions > 0) {
                        totalClicks.toDouble() / totalImpressions
                    } else 0.0
                    val avgPosition = if (data.rows.isNotEmpty()) {
                        data.rows.map { it.position }.average()
                    } else 0.0
                    val (start, end) = data.dateRange
                        .toFormattedStrings()
                    _uiState.value = AuditDashboardUiState.Success(
                        siteUrl = data.siteUrl,
                        clicks = totalClicks,
                        impressions = totalImpressions,
                        ctr = avgCtr,
                        position = avgPosition,
                        dateRangeLabel = "$start a $end"
                    )
                }
                .onFailure { error ->
                    val message = when (error) {
                        is QuotaExceededException -> error.userMessage
                        else -> error.message
                            ?: "Error al obtener métricas"
                    }
                    _uiState.value = AuditDashboardUiState.Error(
                        message = message
                    )
                }
        }
    }
}
