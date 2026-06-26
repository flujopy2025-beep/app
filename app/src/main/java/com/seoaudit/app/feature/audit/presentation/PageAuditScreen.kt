package com.seoaudit.app.feature.audit.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seoaudit.app.core.domain.model.AuditIssue
import com.seoaudit.app.core.domain.model.AuditReport
import com.seoaudit.app.core.domain.model.Severity

/**
 * Page Audit detail screen showing audit results for a specific page.
 * Displays score ring, issues grouped by severity, and generate fix buttons.
 *
 * @param pageId The ID of the page being audited.
 * @param onNavigateBack Callback to navigate back.
 * @param report The audit report to display (null while loading).
 * @param isLoading Whether the audit is currently loading.
 * @param onGenerateFix Callback when user taps "Generate Fix" for an issue.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageAuditScreen(
    pageId: Long,
    onNavigateBack: () -> Unit,
    report: AuditReport? = null,
    isLoading: Boolean = false,
    onGenerateFix: (AuditIssue) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría de Página") },
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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            report != null -> {
                PageAuditContent(
                    report = report,
                    onGenerateFix = onGenerateFix,
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay datos de auditoría para página #$pageId",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}



/**
 * Main content of the page audit screen: score display and issues list.
 */
@Composable
private fun PageAuditContent(
    report: AuditReport,
    onGenerateFix: (AuditIssue) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedIssues = report.issues.groupBy { it.severity }
    val sortedSeverities = listOf(Severity.CRITICAL, Severity.WARNING, Severity.INFO)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ScoreSection(score = report.score, pageTitle = report.pageTitle)
        }

        item {
            IssueSummaryRow(report = report)
            Spacer(modifier = Modifier.height(8.dp))
        }

        sortedSeverities.forEach { severity ->
            val issues = groupedIssues[severity] ?: emptyList()
            if (issues.isNotEmpty()) {
                item {
                    SeverityHeader(severity = severity, count = issues.size)
                }
                items(issues, key = { it.id }) { issue ->
                    IssueCard(issue = issue, onGenerateFix = onGenerateFix)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}



/**
 * Score section with colored ring indicator (0-100).
 */
@Composable
private fun ScoreSection(score: Int, pageTitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = pageTitle,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(16.dp))
        ScoreRing(score = score)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = scoreLabel(score),
            style = MaterialTheme.typography.labelLarge,
            color = scoreColor(score)
        )
    }
}

/**
 * Circular score ring displaying the audit score 0-100.
 */
@Composable
private fun ScoreRing(score: Int, modifier: Modifier = Modifier) {
    val color = scoreColor(score)
    val sweepAngle = (score / 100f) * 360f
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            // Score arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$score",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}



/**
 * Summary row showing counts per severity.
 */
@Composable
private fun IssueSummaryRow(report: AuditReport) {
    val criticalCount = report.issues.count { it.severity == Severity.CRITICAL }
    val warningCount = report.issues.count { it.severity == Severity.WARNING }
    val infoCount = report.issues.count { it.severity == Severity.INFO }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SeverityBadge(label = "Críticos", count = criticalCount, color = Color(0xFFD32F2F))
        SeverityBadge(label = "Advertencias", count = warningCount, color = Color(0xFFF57C00))
        SeverityBadge(label = "Info", count = infoCount, color = Color(0xFF1976D2))
    }
}

/**
 * Small badge showing severity count.
 */
@Composable
private fun SeverityBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Header for a severity group.
 */
@Composable
private fun SeverityHeader(severity: Severity, count: Int) {
    val (label, color) = when (severity) {
        Severity.CRITICAL -> "Problemas Críticos" to Color(0xFFD32F2F)
        Severity.WARNING -> "Advertencias" to Color(0xFFF57C00)
        Severity.INFO -> "Información" to Color(0xFF1976D2)
    }
    Text(
        text = "$label ($count)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.padding(top = 8.dp)
    )
}



/**
 * Card displaying a single audit issue with title, description, impact,
 * recommendation, and optionally a "Generate Fix" button.
 */
@Composable
private fun IssueCard(
    issue: AuditIssue,
    onGenerateFix: (AuditIssue) -> Unit
) {
    val borderColor = when (issue.severity) {
        Severity.CRITICAL -> Color(0xFFD32F2F)
        Severity.WARNING -> Color(0xFFF57C00)
        Severity.INFO -> Color(0xFF1976D2)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = issue.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            IssueDetailRow(label = "Impacto", value = issue.impact)
            Spacer(modifier = Modifier.height(4.dp))
            IssueDetailRow(label = "Recomendación", value = issue.recommendation)

            if (issue.fixable) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onGenerateFix(issue) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generar Corrección")
                }
            }
        }
    }
}

/**
 * A label-value row for issue details.
 */
@Composable
private fun IssueDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}



/**
 * Returns a color based on the audit score value.
 */
private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF2E7D32) // Green — good
    score >= 50 -> Color(0xFFF57C00) // Orange — needs work
    else -> Color(0xFFD32F2F)        // Red — poor
}

/**
 * Returns a human-readable label for the score range.
 */
private fun scoreLabel(score: Int): String = when {
    score >= 80 -> "Buen estado SEO"
    score >= 50 -> "Necesita mejoras"
    else -> "Estado crítico"
}
