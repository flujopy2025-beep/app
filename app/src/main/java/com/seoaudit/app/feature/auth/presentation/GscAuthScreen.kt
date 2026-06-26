package com.seoaudit.app.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * State representing the GSC authentication flow.
 */
sealed interface GscAuthUiState {
    data object Idle : GscAuthUiState
    data object Loading : GscAuthUiState
    data class Success(val email: String, val siteCount: Int) : GscAuthUiState
    data class Error(val message: String) : GscAuthUiState
}

/**
 * Google Search Console authentication screen.
 * Presents the Google Sign-In flow with scope: webmasters.readonly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GscAuthScreen(
    onNavigateBack: () -> Unit,
    uiState: GscAuthUiState = GscAuthUiState.Idle,
    onSignInClicked: () -> Unit = {},
    onRetryClicked: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Search Console") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (uiState) {
                is GscAuthUiState.Idle -> IdleContent(onSignInClicked)
                is GscAuthUiState.Loading -> LoadingContent()
                is GscAuthUiState.Success -> SuccessContent(uiState)
                is GscAuthUiState.Error -> ErrorContent(
                    uiState,
                    onRetryClicked
                )
            }
        }
    }
}

@Composable
private fun IdleContent(onSignInClicked: () -> Unit) {
    Text(
        text = "Conectar Google Search Console",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Inicia sesion con tu cuenta de Google para " +
            "acceder a los datos de rendimiento SEO.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = onSignInClicked,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Iniciar sesion con Google")
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Permiso: solo lectura (webmasters.readonly)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Autenticando...",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun SuccessContent(state: GscAuthUiState.Success) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "Conectado",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Conectado exitosamente",
        style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = state.email,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "${state.siteCount} sitio(s) disponible(s)",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ErrorContent(
    state: GscAuthUiState.Error,
    onRetryClicked: () -> Unit
) {
    Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = "Error",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Error de autenticacion",
        style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = state.message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onRetryClicked) {
        Text("Reintentar")
    }
}
