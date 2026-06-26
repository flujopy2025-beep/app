package com.seoaudit.app.feature.codegen.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Confirmation dialog asking the user to apply a code fix.
 * Shows a summary of changes (additions/deletions count),
 * optional risk warning, and Confirm/Cancel buttons.
 *
 * @param additions Number of lines added by the fix.
 * @param deletions Number of lines deleted by the fix.
 * @param riskWarning Optional warning message if the fix is risky.
 * @param onConfirm Callback when user confirms applying the fix.
 * @param onDismiss Callback when user cancels or dismisses.
 */
@Composable
fun ApplyFixConfirmationDialog(
    additions: Int,
    deletions: Int,
    riskWarning: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "¿Aplicar esta corrección?",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            ApplyFixDialogContent(
                additions = additions,
                deletions = deletions,
                riskWarning = riskWarning
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}



/**
 * Content of the apply fix dialog: change summary and optional risk warning.
 */
@Composable
private fun ApplyFixDialogContent(
    additions: Int,
    deletions: Int,
    riskWarning: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Resumen de cambios:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        ChangeSummaryRow(additions = additions, deletions = deletions)

        if (riskWarning != null) {
            Spacer(modifier = Modifier.height(12.dp))
            RiskWarningSection(warning = riskWarning)
        }
    }
}

/**
 * Row displaying additions and deletions count with colored text.
 */
@Composable
private fun ChangeSummaryRow(additions: Int, deletions: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "+$additions líneas añadidas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1B5E20),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "-$deletions líneas eliminadas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB71C1C),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Risk warning section with icon and message.
 */
@Composable
private fun RiskWarningSection(warning: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Advertencia de riesgo",
            tint = Color(0xFFF57C00)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = warning,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFF57C00)
        )
    }
}
