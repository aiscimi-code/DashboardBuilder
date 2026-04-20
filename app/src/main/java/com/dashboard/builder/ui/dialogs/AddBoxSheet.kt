package com.dashboard.builder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dashboard.builder.data.model.BoxType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBoxSheet(
    onDismiss: () -> Unit,
    onBoxTypeSelected: (BoxType) -> Unit
) {
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
                        onClick = { onBoxTypeSelected(boxType) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
        }
    }
}