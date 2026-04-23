package com.dashboard.builder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Export / Import dialog.
 *
 * All file I/O happens outside this composable via the four callbacks —
 * the caller registers SAF launchers (CreateDocument / OpenDocument) and
 * invokes them from these lambdas. This keeps the dialog pure-UI with no
 * Context dependency.
 */
@Composable
fun ExportImportDialog(
    onDismiss: () -> Unit,
    onExportAll: () -> Unit,
    onExportCurrentTab: () -> Unit,
    onImportAll: () -> Unit,
    onImportCurrentTab: () -> Unit,
    onRestore: (() -> Unit)? = null,
    restoreLabel: String? = null
) {
    var showRestoreDialog by remember { mutableStateOf(false) }

    if (showRestoreDialog && restoreLabel != null) {
        RestoreBackupDialog(
            onDismiss = { showRestoreDialog = false },
            onRestore = { filename ->
                onRestore?.invoke()
                showRestoreDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export / Import Layout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // ── Export ──────────────────────────────────────────────
                Text("Export", style = MaterialTheme.typography.titleSmall)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onExportAll(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("All tabs", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { onExportCurrentTab(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Current tab", style = MaterialTheme.typography.labelMedium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ── Import ──────────────────────────────────────────────
                Text("Import", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Pick a previously exported .json file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onImportAll(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("All tabs", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { onImportCurrentTab(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Single tab", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // ── Restore ─────────────────────────────────────────────
                if (onRestore != null && restoreLabel != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Restore", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Restore from an automatic backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(restoreLabel, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/**
 * Dialog showing available backups for restore.
 */
@Composable
fun RestoreBackupDialog(
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    // This would need to be populated from the ViewModel
    // For now showing a placeholder - the parent will handle actual data
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Backup") },
        text = {
            Text("Select a backup to restore. This will replace your current dashboard.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}