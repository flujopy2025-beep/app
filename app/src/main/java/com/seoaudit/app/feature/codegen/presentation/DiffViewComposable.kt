package com.seoaudit.app.feature.codegen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seoaudit.app.core.domain.model.DiffHunk
import com.seoaudit.app.core.domain.model.DiffLine
import com.seoaudit.app.core.domain.model.DiffLineType
import com.seoaudit.app.core.domain.model.DiffResult

/**
 * Composable that displays a diff with colored lines:
 * - Green background for additions
 * - Red background for removals
 * - Grey background for context lines
 *
 * Shows line numbers in a monospace font and provides
 * Accept/Reject buttons at the bottom.
 *
 * @param diffResult The diff data to display.
 * @param onAccept Callback when user accepts the changes.
 * @param onReject Callback when user rejects the changes.
 * @param modifier Optional modifier.
 */
@Composable
fun DiffViewComposable(
    diffResult: DiffResult,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        DiffHeader(diffResult = diffResult)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            diffResult.hunks.forEachIndexed { index, hunk ->
                item(key = "hunk_header_$index") {
                    HunkHeader(hunk = hunk)
                }
                items(
                    items = hunk.lines,
                    key = { "${index}_${it.lineNumber}_${it.type}" }
                ) { line ->
                    DiffLineRow(line = line)
                }
                item(key = "hunk_divider_$index") {
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }

        DiffActionButtons(onAccept = onAccept, onReject = onReject)
    }
}



/**
 * Header showing additions/deletions summary.
 */
@Composable
private fun DiffHeader(diffResult: DiffResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Vista de Diferencias",
            style = MaterialTheme.typography.titleMedium
        )
        Row {
            Text(
                text = "+${diffResult.additions}",
                color = DiffColors.additionText,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "-${diffResult.deletions}",
                color = DiffColors.removalText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
    HorizontalDivider()
}

/**
 * Header for a specific hunk showing line range.
 */
@Composable
private fun HunkHeader(hunk: DiffHunk) {
    Text(
        text = "@@ -${hunk.oldStart},${hunk.oldLines} +${hunk.newStart},${hunk.newLines} @@",
        modifier = Modifier
            .fillMaxWidth()
            .background(DiffColors.hunkHeaderBg)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}



/**
 * A single line in the diff with line number and colored background.
 */
@Composable
private fun DiffLineRow(line: DiffLine) {
    val backgroundColor = when (line.type) {
        DiffLineType.ADD -> DiffColors.additionBg
        DiffLineType.REMOVE -> DiffColors.removalBg
        DiffLineType.CONTEXT -> DiffColors.contextBg
    }
    val textColor = when (line.type) {
        DiffLineType.ADD -> DiffColors.additionText
        DiffLineType.REMOVE -> DiffColors.removalText
        DiffLineType.CONTEXT -> Color.Unspecified
    }
    val prefix = when (line.type) {
        DiffLineType.ADD -> "+"
        DiffLineType.REMOVE -> "-"
        DiffLineType.CONTEXT -> " "
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 1.dp)
    ) {
        // Line number
        Text(
            text = line.lineNumber.toString().padStart(4),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .width(40.dp)
        )
        // Prefix (+/-/space)
        Text(
            text = prefix,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = textColor,
            modifier = Modifier.padding(end = 4.dp)
        )
        // Content
        Text(
            text = line.content,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = textColor
        )
    }
}



/**
 * Accept and Reject action buttons at the bottom of the diff view.
 */
@Composable
private fun DiffActionButtons(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Rechazar")
        }
        Button(
            onClick = onAccept,
            modifier = Modifier.weight(1f)
        ) {
            Text("Aceptar")
        }
    }
}

/**
 * Color constants for the diff view.
 */
private object DiffColors {
    val additionBg = Color(0xFFE6FFEC)
    val additionText = Color(0xFF1B5E20)
    val removalBg = Color(0xFFFFEBEE)
    val removalText = Color(0xFFB71C1C)
    val contextBg = Color(0xFFF5F5F5)
    val hunkHeaderBg = Color(0xFFE3F2FD)
}
