package com.dashboard.builder.ui.components.boxes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dashboard.builder.data.model.*

@Composable
fun BoxContent(
    box: Box,
    isSelected: Boolean,
    isMoveMode: Boolean = false,
    onInputChange: (String) -> Unit = {},
    onCheckboxToggle: (Int) -> Unit = {}
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(box.style.backgroundColor))
    } catch (e: Exception) {
        Color.White
    }

    val borderColor = if (isSelected) Color(0xFF2196F3) else Color.Gray
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .border(borderWidth, borderColor)
            .padding(4.dp)
    ) {
        when (box.type) {
            BoxType.INPUT -> InputBoxContent(box, isSelected, isMoveMode, onInputChange)
            BoxType.TEXT -> TextBoxContent(box, isSelected, isMoveMode, onInputChange)
            BoxType.BUTTON -> ButtonBoxContent(box, isMoveMode)
            BoxType.CHECKBOX_LIST -> CheckboxListContent(box, isSelected, isMoveMode, onCheckboxToggle)
            BoxType.COUNTER -> CounterContent(box, isSelected, isMoveMode)
        }
    }
}

@Composable
private fun InputBoxContent(box: Box, isSelected: Boolean, isMoveMode: Boolean = false, onInputChange: (String) -> Unit = {}) {
    val config = box.config as? InputConfig ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        // Only show label when selected
        if (isSelected && box.label.isNotEmpty()) {
            Text(
                text = box.label,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        BasicTextField(
            value = config.value,
            onValueChange = onInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            enabled = !isMoveMode, // Disable input in move mode
            textStyle = TextStyle(fontSize = 12.sp),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                Box {
                    if (config.value.isEmpty()) {
                        Text(
                            text = config.placeholder.ifEmpty { "Input..." },
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun TextBoxContent(box: Box, isSelected: Boolean, isMoveMode: Boolean = false, onInputChange: (String) -> Unit = {}) {
    val config = box.config as? TextConfig ?: return
    var isEditing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Only show label when selected
        if (isSelected && box.label.isNotEmpty()) {
            Text(
                text = box.label,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        
        if (isSelected && !isMoveMode) {
            // Editable mode - show text field when selected (and not in move mode)
            BasicTextField(
                value = config.value,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(fontSize = 12.sp),
                cursorBrush = SolidColor(Color.Black),
                decorationBox = { innerTextField ->
                    Box {
                        if (config.value.isEmpty()) {
                            Text(
                                text = "Tap to edit...",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        } else {
            // View mode - just display text
            Text(
                text = config.value,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ButtonBoxContent(box: Box, isMoveMode: Boolean = false) {
    val config = box.config as? ButtonConfig ?: return

    // Buttons don't show labels - only the button text
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { /* Handled by ViewModel */ },
            enabled = !isMoveMode, // Disable button in move mode
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(config.text, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CheckboxListContent(box: Box, isSelected: Boolean, isMoveMode: Boolean = false, onCheckboxToggle: (Int) -> Unit = {}) {
    val config = box.config as? CheckboxListConfig ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        // Only show label when selected
        if (isSelected && box.label.isNotEmpty()) {
            Text(
                text = box.label,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        if (config.items.isEmpty()) {
            Text(
                text = "Empty list",
                color = Color.Gray,
                fontSize = 10.sp
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                config.items.forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 1.dp)
                            .clickable(enabled = !isMoveMode) { onCheckboxToggle(index) }
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { onCheckboxToggle(index) },
                            enabled = !isMoveMode, // Disable in move mode
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item.text,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterContent(box: Box, isSelected: Boolean, isMoveMode: Boolean = false) {
    val config = box.config as? CounterConfig ?: return

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Only show label when selected
        if (isSelected && box.label.isNotEmpty()) {
            Text(
                text = box.label,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { /* Handled by ViewModel */ },
                enabled = !isMoveMode, // Disable in move mode
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = config.value.toString(),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { /* Handled by ViewModel */ },
                enabled = !isMoveMode, // Disable in move mode
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}