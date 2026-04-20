package com.dashboard.builder.ui.components.boxes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    isSelected: Boolean
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
            BoxType.INPUT -> InputBoxContent(box)
            BoxType.TEXT -> TextBoxContent(box)
            BoxType.BUTTON -> ButtonBoxContent(box)
            BoxType.CHECKBOX_LIST -> CheckboxListContent(box)
            BoxType.COUNTER -> CounterContent(box)
        }
    }
}

@Composable
private fun InputBoxContent(box: Box) {
    val config = box.config as? InputConfig ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        if (box.label.isNotEmpty()) {
            Text(
                text = box.label,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        BasicTextField(
            value = config.value,
            onValueChange = { /* Handled by ViewModel */ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
private fun TextBoxContent(box: Box) {
    val config = box.config as? TextConfig ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        if (box.label.isNotEmpty()) {
            Text(
                text = box.label,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Text(
            text = config.value,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ButtonBoxContent(box: Box) {
    val config = box.config as? ButtonConfig ?: return

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (box.label.isNotEmpty()) {
            Text(
                text = box.label,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Button(
            onClick = { /* Handled by ViewModel */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(config.text, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CheckboxListContent(box: Box) {
    val config = box.config as? CheckboxListConfig ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        if (box.label.isNotEmpty()) {
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
                config.items.take(5).forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { /* Handled by ViewModel */ },
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
private fun CounterContent(box: Box) {
    val config = box.config as? CounterConfig ?: return

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (box.label.isNotEmpty()) {
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