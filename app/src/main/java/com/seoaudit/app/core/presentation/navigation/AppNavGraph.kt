package com.seoaudit.app.core.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seoaudit.app.core.presentation.components.HomeScreen
import com.seoaudit.app.feature.ai.presentation.AiChatScreen
import com.seoaudit.app.feature.audit.presentation.AuditDashboardScreen
import com.seoaudit.app.feature.audit.presentation.PageAuditScreen
import com.seoaudit.app.feature.auth.presentation.GscAuthScreen
import com.seoaudit.app.feature.auth.presentation.WpAuthScreen
import com.seoaudit.app.feature.diagnostic.presentation.DiagnosticReportScreen
import com.seoaudit.app.feature.filesystem.presentation.FileExplorerScreen
import com.seoaudit.app.feature.mcp.presentation.McpConsoleScreen
import com.seoaudit.app.feature.settings.presentation.SettingsScreen

/**
 * Main navigation graph for the SEO Audit app.
 *
 * Defines all composable destinations and their navigation relationships.
 * Preserves back stack state for backward navigation (Requirement 18.4).
 *
 * @param navController The navigation controller to use.
 * @param startDestination The initial route to display.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGscAuth = { navController.navigate(Screen.GscAuth.route) },
                onNavigateToWpAuth = { navController.navigate(Screen.WpAuth.route) },
                onNavigateToAudit = { navController.navigate(Screen.AuditDashboard.route) },
                onNavigateToFiles = { navController.navigate(Screen.FileExplorer.route) },
                onNavigateToDiagnostic = { navController.navigate(Screen.DiagnosticReport.route) },
                onNavigateToMcp = { navController.navigate(Screen.McpConsole.route) },
                onNavigateToAiChat = { navController.navigate(Screen.AiChat.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.GscAuth.route) {
            GscAuthScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignInClicked = { /* Triggered from ViewModel in production */ },
                onRetryClicked = { /* Triggered from ViewModel in production */ }
            )
        }

        composable(Screen.WpAuth.route) {
            WpAuthScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AuditDashboard.route) {
            AuditDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPageAudit = { pageId ->
                    navController.navigate(Screen.PageAudit.createRoute(pageId))
                }
            )
        }

        composable(
            route = Screen.PageAudit.route,
            arguments = listOf(navArgument("pageId") { type = NavType.LongType })
        ) { backStackEntry ->
            val pageId = backStackEntry.arguments?.getLong("pageId") ?: return@composable
            PageAuditScreen(
                pageId = pageId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FileExplorer.route) {
            FileExplorerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DiagnosticReport.route) {
            DiagnosticReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.McpConsole.route) {
            McpConsoleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AiChat.route) {
            AiChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Reusable placeholder screen layout for screens not yet implemented.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title - En desarrollo",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
