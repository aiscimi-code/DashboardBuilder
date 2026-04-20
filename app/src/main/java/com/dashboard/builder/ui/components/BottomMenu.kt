package com.dashboard.builder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
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
    onMoveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportClick: () -> Unit,
    onSaveClick: () -> Unit,
    onUndoClick: () -> Unit,
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
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
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
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }

            // Move button
            FilledTonalButton(
                onClick = onMoveClick,
                enabled = hasSelection,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (currentMode == EditMode.MOVE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.OpenWith, contentDescription = "Move")
            }

            // Delete button
            FilledTonalButton(
                onClick = onDeleteClick,
                enabled = hasSelection,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }

            // Export button
            FilledTonalButton(
                onClick = onExportClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export")
            }
            // Save button
            FilledTonalButton(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
            // Undo button
            FilledTonalButton(
                onClick = onUndoClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo")
            }
        }
    }
}