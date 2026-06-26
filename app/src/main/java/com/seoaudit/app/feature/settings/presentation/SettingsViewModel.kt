package com.seoaudit.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seoaudit.app.core.domain.model.ServiceType
import com.seoaudit.app.core.domain.repository.CredentialRepository
import com.seoaudit.app.feature.auth.domain.usecase.DisconnectAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val disconnectAccountUseCase: DisconnectAccountUseCase,
    private val credentialRepository: CredentialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadConnectionStatus()
    }

    private fun loadConnectionStatus() {
        viewModelScope.launch {
            val gscConnected = credentialRepository.exists(ServiceType.GSC.credentialKey)
            val wpConnected = credentialRepository.exists(ServiceType.WORDPRESS.credentialKey)
            val geminiSet = credentialRepository.exists(ServiceType.GEMINI.credentialKey)
            val claudeSet = credentialRepository.exists(ServiceType.CLAUDE.credentialKey)

            _uiState.update {
                it.copy(
                    isGscConnected = gscConnected,
                    isWordPressConnected = wpConnected,
                    isGeminiKeySet = geminiSet,
                    isClaudeKeySet = claudeSet
                )
            }
        }
    }

    fun showDisconnectDialog(target: DisconnectDialogTarget) {
        _uiState.update { it.copy(showDisconnectDialog = target) }
    }

    fun dismissDisconnectDialog() {
        _uiState.update { it.copy(showDisconnectDialog = null) }
    }

    fun confirmDisconnect() {
        val target = _uiState.value.showDisconnectDialog ?: return
        _uiState.update { it.copy(showDisconnectDialog = null, isDisconnecting = true) }

        viewModelScope.launch {
            val result = when (target) {
                DisconnectDialogTarget.GSC -> disconnectAccountUseCase.disconnectGsc()
                DisconnectDialogTarget.WORDPRESS ->
                    disconnectAccountUseCase.disconnectWordPress(_uiState.value.wpSiteUrl)
                DisconnectDialogTarget.ALL -> disconnectAccountUseCase.disconnectAll()
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDisconnecting = false,
                            successMessage = "Cuenta desconectada exitosamente"
                        )
                    }
                    loadConnectionStatus()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDisconnecting = false,
                            errorMessage = error.message ?: "Error al desconectar"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
