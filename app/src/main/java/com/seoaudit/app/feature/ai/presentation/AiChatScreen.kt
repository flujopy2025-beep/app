package com.seoaudit.app.feature.ai.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * AI Chat screen with conversational UI for the
 * interactive discovery flow.
 *
 * Displays system questions and user responses as chat
 * bubbles with a text input at the bottom.
 * Material Design 3 styling optimized for 6" screens.
 *
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 18.1, 18.2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría SEO - Chat IA") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Chat messages list
            ChatMessagesList(
                messages = uiState.messages,
                isAnalyzing = uiState.isAnalyzing,
                modifier = Modifier.weight(1f)
            )

            // Input field at bottom
            ChatInputBar(
                inputText = uiState.inputText,
                onInputChanged = viewModel::onInputChanged,
                onSend = viewModel::onSendMessage,
                enabled = !uiState.isAnalyzing,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
