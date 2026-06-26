package com.seoaudit.app.core.presentation.navigation

/**
 * Sealed class defining all navigation routes in the SEO Audit app.
 *
 * Each screen is represented as a data object with a unique route string.
 * Screens with parameters (like PageAudit) provide a factory method to create the route.
 */
sealed class Screen(val route: String) {

    data object Home : Screen("home")

    data object GscAuth : Screen("gsc_auth")

    data object WpAuth : Screen("wp_auth")

    data object AuditDashboard : Screen("audit_dashboard")

    data object PageAudit : Screen("page_audit/{pageId}") {
        fun createRoute(pageId: Long) = "page_audit/$pageId"
    }

    data object FileExplorer : Screen("file_explorer")

    data object DiagnosticReport : Screen("diagnostic_report")

    data object McpConsole : Screen("mcp_console")

    data object AiChat : Screen("ai_chat")

    data object Settings : Screen("settings")
}
