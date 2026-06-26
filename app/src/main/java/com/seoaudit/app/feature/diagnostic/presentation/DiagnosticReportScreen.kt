package com.seoaudit.app.feature.diagnostic.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seoaudit.app.core.domain.model.DiagnosticProblem
import com.seoaudit.app.core.domain.model.DiagnosticReport
import com.seoaudit.app.core.domain.model.ImpactLevel
import com.seoaudit.app.core.domain.model.ProblemOrigin
import com.seoaudit.app.core.domain.model.TechnicalCategory

private val HighImpactColor = Color(0xFFD32F2F)
private val MediumImpactColor = Color(0xFFF57C00)
private val LowImpactColor = Color(0xFF757575)
private val GscBadgeColor = Color(0xFF1565C0)
private val CodeBadgeColor = Color(0xFF2E7D32)

/**
 * Diagnostic Report screen showing the diagnostic table
 * with problems sorted by impact and optionally grouped
 * by category when there are more than 10 problems.
 *
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticReportScreen(
    report: DiagnosticReport?,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informe Diagnóstico") },
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
        if (report == null || report.problems.isEmpty()) {
            EmptyReportContent(
                modifier = Modifier.padding(padding)
            )
        } else {
            DiagnosticReportContent(
                report = report,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun EmptyReportContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se encontraron problemas.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DiagnosticReportContent(
    report: DiagnosticReport,
    modifier: Modifier = Modifier
) {
    val shouldGroup = report.problems.size > 10

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ReportHeader(
                totalProblems = report.problems.size
            )
        }

        if (shouldGroup) {
            val grouped = report.problems.groupBy { it.category }
            grouped.forEach { (category, problems) ->
                item {
                    CategoryHeader(category = category)
                }
                items(problems) { problem ->
                    ProblemCard(problem = problem)
                }
            }
        } else {
            items(report.problems) { problem ->
                ProblemCard(problem = problem)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ReportHeader(totalProblems: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "$totalProblems problemas detectados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}

@Composable
private fun CategoryHeader(category: TechnicalCategory) {
    Text(
        text = categoryDisplayName(category),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ProblemCard(problem: DiagnosticProblem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Row 1: Impact badge + Origin badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImpactBadge(impact = problem.impact)
                OriginBadge(origin = problem.origin)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Problem description
            Text(
                text = problem.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Proposed solution
            Text(
                text = problem.proposedSolution,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImpactBadge(impact: ImpactLevel) {
    val (color, label) = when (impact) {
        ImpactLevel.HIGH -> HighImpactColor to "Alto"
        ImpactLevel.MEDIUM -> MediumImpactColor to "Medio"
        ImpactLevel.LOW -> LowImpactColor to "Bajo"
    }
    Badge(text = label, backgroundColor = color)
}

@Composable
private fun OriginBadge(origin: ProblemOrigin) {
    val (color, label) = when (origin) {
        ProblemOrigin.GSC -> GscBadgeColor to "GSC"
        ProblemOrigin.CODE -> CodeBadgeColor to "Código"
    }
    Badge(text = label, backgroundColor = color)
}

@Composable
private fun Badge(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun categoryDisplayName(
    category: TechnicalCategory
): String {
    return when (category) {
        TechnicalCategory.PERFORMANCE -> "Rendimiento"
        TechnicalCategory.HTML_STRUCTURE -> "Estructura HTML"
        TechnicalCategory.STRUCTURED_DATA -> "Datos Estructurados"
        TechnicalCategory.BLOCKING_RESOURCES -> "Recursos Bloqueantes"
    }
}
