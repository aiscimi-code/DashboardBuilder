package com.dashboard.builder.ui.dialogs

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExportImportDialog(
    context: Context,
    onDismiss: () -> Unit,
    onExport: (targetAll: Boolean) -> Unit,
    onImport: (targetAll: Boolean, jsonContent: String) -> Unit
) {
    var exportAll by remember { mutableStateOf(true) }
    var importAll by remember { mutableStateOf(true) }
    var importText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export / Import Layout") },
        text = {
            Column {
                // Export options
                Text("Export:")
                Button(
                    onClick = { onExport(exportAll) },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(if (exportAll) "Export All Tabs" else "Export Current Tab")
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Import options
                Text("Import (paste JSON below):")
                BasicTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    modifier = Modifier
                        .padding(4.dp)
                        .height(120.dp)
                )
                Button(
                    onClick = { onImport(importAll, importText) },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(if (importAll) "Import All Tabs" else "Import Current Tab")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
