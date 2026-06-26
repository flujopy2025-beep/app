package com.seoaudit.app.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Main Home Screen with navigation cards to all sections.
 *
 * Optimized for scannability on 6-inch screens (Requirement 18.2).
 * Uses a 2-column grid layout for quick access to main actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGscAuth: () -> Unit,
    onNavigateToWpAuth: () -> Unit,
    onNavigateToAudit: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToDiagnostic: () -> Unit,
    onNavigateToMcp: () -> Unit,
    onNavigateToAiChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SEO Audit Tool",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configuracion"
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Panel Principal",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selecciona una seccion para comenzar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    NavigationCard(
                        title = "Google Search Console",
                        description = "Conectar cuenta GSC",
                        icon = Icons.Filled.Analytics,
                        onClick = onNavigateToGscAuth
                    )
                }
                item {
                    NavigationCard(
                        title = "WordPress",
                        description = "Conectar sitio WP",
                        icon = Icons.Filled.Cloud,
                        onClick = onNavigateToWpAuth
                    )
                }
                item {
                    NavigationCard(
                        title = "Auditoria SEO",
                        description = "Analizar paginas",
                        icon = Icons.Filled.Description,
                        onClick = onNavigateToAudit
                    )
                }
                item {
                    NavigationCard(
                        title = "Archivos",
                        description = "Explorar proyecto local",
                        icon = Icons.Filled.Folder,
                        onClick = onNavigateToFiles
                    )
                }
                item {
                    NavigationCard(
                        title = "Diagnostico",
                        description = "Informe de problemas",
                        icon = Icons.Filled.SmartToy,
                        onClick = onNavigateToDiagnostic
                    )
                }
                item {
                    NavigationCard(
                        title = "MCP Console",
                        description = "Servidor MCP",
                        icon = Icons.Filled.Terminal,
                        onClick = onNavigateToMcp
                    )
                }
                item {
                    NavigationCard(
                        title = "Chat IA",
                        description = "Asistente inteligente",
                        icon = Icons.Filled.Chat,
                        onClick = onNavigateToAiChat
                    )
                }
                item {
                    NavigationCard(
                        title = "Configuracion",
                        description = "Ajustes de la app",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToSettings
                    )
                }
            }
        }
    }
}

/**
 * Navigation card component for the home screen grid.
 *
 * Provides a touch target optimized for mobile interaction
 * with an icon, title, and description.
 */
@Composable
fun NavigationCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
