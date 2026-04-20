package com.dashboard.builder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dashboard.builder.viewmodel.EditMode

@Composable
fun BottomMenu(
    currentMode: EditMode,
    hasSelection: Boolean,
    onAddClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Add button
            FilledTonalButton(
                onClick = onAddClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (currentMode == EditMode.ADD)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }

            // Edit button
            FilledTonalButton(
                onClick = onEditClick,
                enabled = hasSelection,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (currentMode == EditMode.EDIT)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
                Spacer(Modifier.width(4.dp))
                Text("Edit")
            }

            // Delete button
            FilledTonalButton(
                onClick = onDeleteClick,
                enabled = hasSelection
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
                Spacer(Modifier.width(4.dp))
                Text("Delete")
            }

            // Export button
            FilledTonalButton(onClick = onExportClick) {
                Icon(Icons.Default.Share, contentDescription = "Export")
                Spacer(Modifier.width(4.dp))
                Text("Export")
            }
        }
    }
}