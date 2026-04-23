package com.dashboard.builder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dashboard.builder.data.model.BoxType
import com.dashboard.builder.data.model.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBoxSheet(
    onDismiss: () -> Unit,
    onBoxTypeSelected: (BoxType, Int, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf<BoxType?>(null) }
    var boxWidth by remember { mutableStateOf("1") }
    var boxHeight by remember { mutableStateOf("5") }

    if (selectedType == null) {
        // Type selection UI
        ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Add Box",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BoxType.entries.toTypedArray()) { boxType ->
                        BoxTypeItem(
                            boxType = boxType,
                            onClick = { selectedType = boxType }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        // Size configuration UI
        val isButton = selectedType == BoxType.BUTTON
        val maxWidth = if (isButton) 10 else 256
        val maxHeight = 10
        val defaultW = if (isButton) "1" else "1"
        val defaultH = if (isButton) "1" else "5"

        // Reset to defaults when type changes
        boxWidth = defaultW
        boxHeight = defaultH

        ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header with back button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    IconButton(onClick = { selectedType = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Configure ${selectedType?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // Size configuration
                Text(
                    text = "Size",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = boxWidth,
                        onValueChange = { 
                            boxWidth = it.filter { c -> c.isDigit() }.take(3)
                        },
                        label = { Text("Width (1-$maxWidth)") },
                        modifier = Modifier.weight(1f),
                        supportingText = {
                            Text("min: 1, max: $maxWidth")
                        }
                    )
                    OutlinedTextField(
                        value = boxHeight,
                        onValueChange = { 
                            boxHeight = it.filter { c -> c.isDigit() }.take(2)
                        },
                        label = { Text("Height (1-$maxHeight)") },
                        modifier = Modifier.weight(1f),
                        supportingText = {
                            Text("min: 1, max: $maxHeight")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add button
                Button(
                    onClick = {
                        val w = boxWidth.toIntOrNull()?.coerceIn(1, maxWidth) ?: 1
                        val h = boxHeight.toIntOrNull()?.coerceIn(1, maxHeight) ?: 1
                        selectedType?.let { type ->
                            onBoxTypeSelected(type, w, h)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add ${selectedType?.name?.lowercase()?.replaceFirstChar { it.uppercase() }}")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun BoxTypeItem(
    boxType: BoxType,
    onClick: () -> Unit
) {
    val (icon, label, description) = when (boxType) {
        BoxType.INPUT -> Triple(Icons.Default.TextFields, "Input Box", "Text input field")
        BoxType.TEXT -> Triple(Icons.Default.TextSnippet, "Text Display", "Read-only text")
        BoxType.BUTTON -> Triple(Icons.Default.TouchApp, "Button", "Clickable action button")
        BoxType.CHECKBOX_LIST -> Triple(Icons.Default.CheckBox, "Checkbox List", "Todo-style list")
        BoxType.COUNTER -> Triple(Icons.Default.AddCircle, "Counter", "Increment/decrement counter")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}