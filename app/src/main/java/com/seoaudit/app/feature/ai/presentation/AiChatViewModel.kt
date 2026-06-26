package com.seoaudit.app.feature.ai.presentation

import androidx.lifecycle.ViewModel
import com.seoaudit.app.feature.ai.domain.model.ChatMessage
import com.seoaudit.app.feature.ai.domain.model.DiscoveryData
import com.seoaudit.app.feature.ai.domain.model.DiscoveryStep
import com.seoaudit.app.feature.ai.domain.usecase.DiscoveryFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI State for the AI Chat discovery screen.
 */
data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val discoveryData: DiscoveryData = DiscoveryData(),
    val isAnalyzing: Boolean = false,
    val inputText: String = ""
)

/**
 * ViewModel managing the interactive discovery chat flow.
 *
 * Orchestrates the step-by-step questions, validates input,
 * and tracks the conversation state.
 */
@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val discoveryFlowUseCase: DiscoveryFlowUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        startDiscoveryFlow()
    }

    private fun startDiscoveryFlow() {
        val question = discoveryFlowUseCase.getQuestionForStep(
            DiscoveryStep.ASK_URL
        )
        val message = ChatMessage(
            content = question,
            isFromUser = false
        )
        _uiState.value = _uiState.value.copy(
            messages = listOf(message)
        )
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onSendMessage() {
        val input = _uiState.value.inputText.trim()
        if (input.isBlank()) return

        val currentState = _uiState.value
        val currentStep = currentState.discoveryData.currentStep

        if (currentStep == DiscoveryStep.COMPLETE) return

        // Add user message
        val userMessage = ChatMessage(
            content = input,
            isFromUser = true
        )
        val updatedMessages = currentState.messages + userMessage

        // Validate input
        val followUp = discoveryFlowUseCase.validateInput(
            currentStep, input
        )

        if (followUp != null) {
            // Input insufficient — ask follow-up
            val followUpMsg = ChatMessage(
                content = followUp,
                isFromUser = false
            )
            _uiState.value = currentState.copy(
                messages = updatedMessages + followUpMsg,
                inputText = ""
            )
            return
        }

        // Process valid response and advance step
        val newData = discoveryFlowUseCase.processResponse(
            currentState.discoveryData, input
        )

        val nextQuestion = discoveryFlowUseCase.getQuestionForStep(
            newData.currentStep
        )
        val systemMsg = ChatMessage(
            content = nextQuestion,
            isFromUser = false
        )

        val isComplete = newData.currentStep == DiscoveryStep.COMPLETE

        _uiState.value = currentState.copy(
            messages = updatedMessages + systemMsg,
            discoveryData = newData,
            isAnalyzing = isComplete,
            inputText = ""
        )
    }
}
