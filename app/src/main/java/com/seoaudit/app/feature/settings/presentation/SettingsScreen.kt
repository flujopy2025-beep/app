package com.seoaudit.app.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


/**
 * Settings screen for managing connected accounts and API keys.
 * Shows connection status for GSC, WordPress, and LLM services.
 * Provides disconnect functionality with confirmation dialogs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuracion") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cuentas Conectadas",
                style = MaterialTheme.typography.titleLarge
            )

            GscConnectionCard(
                isConnected = uiState.isGscConnected,
                isDisconnecting = uiState.isDisconnecting,
                onDisconnect = { viewModel.showDisconnectDialog(DisconnectDialogTarget.GSC) }
            )

            WordPressConnectionCard(
                isConnected = uiState.isWordPressConnected,
                isDisconnecting = uiState.isDisconnecting,
                onDisconnect = { viewModel.showDisconnectDialog(DisconnectDialogTarget.WORDPRESS) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Claves API (LLM)",
                style = MaterialTheme.typography.titleLarge
            )

            LlmApiKeyCard(
                isGeminiSet = uiState.isGeminiKeySet,
                isClaudeSet = uiState.isClaudeKeySet
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Disconnect confirmation dialog
    uiState.showDisconnectDialog?.let { target ->
        DisconnectConfirmationDialog(
            target = target,
            onConfirm = { viewModel.confirmDisconnect() },
            onDismiss = { viewModel.dismissDisconnectDialog() }
        )
    }
}


@Composable
private fun GscConnectionCard(
    isConnected: Boolean,
    isDisconnecting: Boolean,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Google Search Console",
                    style = MaterialTheme.typography.titleMedium
                )
                ConnectionStatusBadge(isConnected = isConnected)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isConnected) "Cuenta vinculada via OAuth 2.0"
                else "No conectado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDisconnect,
                    enabled = !isDisconnecting,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDisconnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Desconectar")
                }
            }
        }
    }
}


@Composable
private fun WordPressConnectionCard(
    isConnected: Boolean,
    isDisconnecting: Boolean,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WordPress",
                    style = MaterialTheme.typography.titleMedium
                )
                ConnectionStatusBadge(isConnected = isConnected)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isConnected) "Conectado via Application Password"
                else "No conectado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDisconnect,
                    enabled = !isDisconnecting,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDisconnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Desconectar")
                }
            }
        }
    }
}


@Composable
private fun LlmApiKeyCard(
    isGeminiSet: Boolean,
    isClaudeSet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Proveedores de IA",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            ApiKeyRow(name = "Gemini API", isSet = isGeminiSet)
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeyRow(name = "Claude API", isSet = isClaudeSet)
        }
    }
}

@Composable
private fun ApiKeyRow(name: String, isSet: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = if (isSet) "Configurada" else "No configurada",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun ConnectionStatusBadge(isConnected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isConnected) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = if (isConnected) "Conectado" else "Desconectado",
            tint = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isConnected) "Conectado" else "Desconectado",
            style = MaterialTheme.typography.labelMedium,
            color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DisconnectConfirmationDialog(
    target: DisconnectDialogTarget,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val serviceName = when (target) {
        DisconnectDialogTarget.GSC -> "Google Search Console"
        DisconnectDialogTarget.WORDPRESS -> "WordPress"
        DisconnectDialogTarget.ALL -> "todas las cuentas"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar desconexion") },
        text = {
            Text(
                "Se eliminaran las credenciales y datos en cache de $serviceName. " +
                    "Esta accion no se puede deshacer."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Desconectar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
