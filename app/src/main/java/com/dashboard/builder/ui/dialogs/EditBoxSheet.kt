package com.dashboard.builder.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dashboard.builder.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBoxSheet(
    box: Box,
    allBoxes: List<Box>,
    onDismiss: () -> Unit,
    onUpdateLabel: (String) -> Unit,
    onUpdateLocked: (Boolean) -> Unit,
    onUpdateBackgroundColor: (String) -> Unit,
    onUpdateConfig: (BoxConfig) -> Unit,
    onUpdateSize: (Int, Int) -> Unit,
    onAddAction: (Action) -> Unit,
    onRemoveAction: (Int) -> Unit
) {
    var label by remember { mutableStateOf(box.label) }
    var locked by remember { mutableStateOf(box.locked) }
    var backgroundColor by remember { mutableStateOf(box.style.backgroundColor) }
    var boxWidth by remember { mutableStateOf(box.size.w.toString()) }
    var boxHeight by remember { mutableStateOf(box.size.h.toString()) }
    var showActionDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Edit ${box.type.name}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Locked toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Locked")
                Switch(
                    checked = locked,
                    onCheckedChange = { locked = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Background color
            OutlinedTextField(
                value = backgroundColor,
                onValueChange = { backgroundColor = it },
                label = { Text("Background Color (hex)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Size controls
            val isButton = box.type == BoxType.BUTTON
            val maxWidth = if (isButton) 10 else 256
            val maxHeight = 10
            
            Text(
                text = "Size",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = boxWidth,
                    onValueChange = { boxWidth = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Width (1-$maxWidth)") },
                    modifier = Modifier.weight(1f),
                    supportingText = { Text("min: 1, max: $maxWidth") }
                )
                OutlinedTextField(
                    value = boxHeight,
                    onValueChange = { boxHeight = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Height (1-$maxHeight)") },
                    modifier = Modifier.weight(1f),
                    supportingText = { Text("min: 1, max: $maxHeight") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type-specific config
            when (val config = box.config) {
                is ButtonConfig -> {
                    OutlinedTextField(
                        value = config.text,
                        onValueChange = { onUpdateConfig(config.copy(text = it)) },
                        label = { Text("Button Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is TextConfig -> {
                    OutlinedTextField(
                        value = config.value,
                        onValueChange = { onUpdateConfig(config.copy(value = it)) },
                        label = { Text("Text Value") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
                is InputConfig -> {
                    OutlinedTextField(
                        value = config.placeholder,
                        onValueChange = { onUpdateConfig(config.copy(placeholder = it)) },
                        label = { Text("Placeholder") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is CounterConfig -> {
                    OutlinedTextField(
                        value = config.value.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> onUpdateConfig(config.copy(value = v)) } },
                        label = { Text("Initial Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is CheckboxListConfig -> {
                    Text("Checkbox items: ${config.items.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Actions section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Actions", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { showActionDialog = true }) {
                    Icon(Icons.Default.Add, "Add Action")
                }
            }

            if (box.actions.isEmpty()) {
                Text(
                    "No actions configured",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                box.actions.forEachIndexed { index, action ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${action.event.name} → ${action.type.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Target: ${allBoxes.find { it.id == action.targetBoxId }?.label ?: action.targetBoxId}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { onRemoveAction(index) }) {
                                Icon(Icons.Default.Delete, "Remove")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    onUpdateLabel(label)
                    onUpdateLocked(locked)
                    onUpdateBackgroundColor(backgroundColor)
                    // Update size with type-specific constraints
                    val isButton = box.type == BoxType.BUTTON
                    val maxW = if (isButton) 10 else 256
                    val maxH = 10
                    val w = boxWidth.toIntOrNull()?.coerceIn(1, maxW) ?: box.size.w
                    val h = boxHeight.toIntOrNull()?.coerceIn(1, maxH) ?: box.size.h
                    if (w != box.size.w || h != box.size.h) {
                        onUpdateSize(w, h)
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Action dialog
    if (showActionDialog) {
        AddActionDialog(
            sourceBoxId = box.id,
            availableBoxes = allBoxes.filter { it.id != box.id },
            onDismiss = { showActionDialog = false },
            onAddAction = { action ->
                onAddAction(action)
                showActionDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddActionDialog(
    sourceBoxId: String,
    availableBoxes: List<Box>,
    onDismiss: () -> Unit,
    onAddAction: (Action) -> Unit
) {
    var selectedEvent by remember { mutableStateOf(EventType.ON_CLICK) }
    var selectedActionType by remember { mutableStateOf(ActionType.SET_TEXT) }
    var selectedTargetBoxId by remember { mutableStateOf(availableBoxes.firstOrNull()?.id ?: "") }
    var useStaticValue by remember { mutableStateOf(false) }
    var staticValue by remember { mutableStateOf("") }
    var dataSourceBoxId by remember { mutableStateOf(availableBoxes.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Action") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Event
                Text("Event", style = MaterialTheme.typography.labelMedium)
                Row {
                    EventType.entries.forEach { event ->
                        FilterChip(
                            selected = selectedEvent == event,
                            onClick = { selectedEvent = event },
                            label = { Text(event.name.removePrefix("ON_")) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action type
                Text("Action", style = MaterialTheme.typography.labelMedium)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedActionType.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ActionType.entries.forEach { actionType ->
                            DropdownMenuItem(
                                text = { Text(actionType.name) },
                                onClick = {
                                    selectedActionType = actionType
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target box
                if (availableBoxes.isNotEmpty()) {
                    Text("Target Box", style = MaterialTheme.typography.labelMedium)
                    var targetExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = targetExpanded,
                        onExpandedChange = { targetExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTargetBoxId,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(targetExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false }
                        ) {
                            availableBoxes.forEach { targetBox ->
                                DropdownMenuItem(
                                    text = { Text("${targetBox.type.name} (${targetBox.id})") },
                                    onClick = {
                                        selectedTargetBoxId = targetBox.id
                                        targetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Data source
                Text("Data Source", style = MaterialTheme.typography.labelMedium)
                Row {
                    FilterChip(
                        selected = !useStaticValue,
                        onClick = { useStaticValue = false },
                        label = { Text("From Box") }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = useStaticValue,
                        onClick = { useStaticValue = true },
                        label = { Text("Static") }
                    )
                }

                if (useStaticValue) {
                    OutlinedTextField(
                        value = staticValue,
                        onValueChange = { staticValue = it },
                        label = { Text("Static Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (availableBoxes.isNotEmpty()) {
                    var sourceExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = sourceExpanded,
                        onExpandedChange = { sourceExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = dataSourceBoxId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Source Box") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sourceExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false }
                        ) {
                            availableBoxes.forEach { srcBox ->
                                DropdownMenuItem(
                                    text = { Text("${srcBox.type.name} (${srcBox.id})") },
                                    onClick = {
                                        dataSourceBoxId = srcBox.id
                                        sourceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dataSource = if (useStaticValue) {
                        DataSource.Static(staticValue)
                    } else {
                        DataSource.FromBox(dataSourceBoxId)
                    }
                    val action = Action(
                        event = selectedEvent,
                        type = selectedActionType,
                        targetBoxId = selectedTargetBoxId,
                        dataSource = dataSource
                    )
                    onAddAction(action)
                },
                enabled = selectedTargetBoxId.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}