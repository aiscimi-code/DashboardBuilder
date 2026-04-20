package com.dashboard.builder.ui.components.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dashboard.builder.data.model.Box
import com.dashboard.builder.data.model.Tab
import com.dashboard.builder.ui.components.boxes.BoxContent

@Composable
fun GridCanvas(
    tab: Tab,
    selectedBoxId: String?,
    cellSize: Dp = 80.dp,
    onBoxSelected: (String?) -> Unit,
    onBoxMoved: (String, Int, Int) -> Unit,
    onBoxResized: (String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSize.toPx() }

    // Calculate grid dimensions
    val gridWidth = GridEngine.COLUMNS * cellSizePx
    val gridHeight = GridEngine.ROWS * cellSizePx

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(android.graphics.Color.parseColor(tab.backgroundColor)))
    ) {
        Box(
            modifier = Modifier
                .width(with(density) { (gridWidth / density.density).toDp() })
                .height(with(density) { (gridHeight / density.density).toDp() })
        ) {
            // Render non-floating boxes
            tab.boxes.filter { !it.floating }.forEach { box ->
                DraggableBoxItem(
                    box = box,
                    cellSize = cellSize,
                    cellSizePx = cellSizePx,
                    isSelected = box.id == selectedBoxId,
                    onSelected = { onBoxSelected(box.id) },
                    onMoved = { dx, dy ->
                        val newX = box.position.x + (dx / cellSizePx).toInt()
                        val newY = box.position.y + (dy / cellSizePx).toInt()
                        onBoxMoved(box.id, newX, newY)
                    },
                    onResized = { dw, dh ->
                        val newW = box.size.w + (dw / cellSizePx).toInt()
                        val newH = box.size.h + (dh / cellSizePx).toInt()
                        onBoxResized(box.id, newW, newH)
                    }
                )
            }

            // Render floating boxes (on top)
            tab.boxes.filter { it.floating }.forEach { box ->
                BoxItem(
                    box = box,
                    cellSize = cellSize,
                    isSelected = box.id == selectedBoxId,
                    onSelected = { onBoxSelected(box.id) }
                )
            }
        }
    }
}

@Composable
private fun DraggableBoxItem(
    box: Box,
    cellSize: Dp,
    cellSizePx: Float,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onMoved: (Float, Float) -> Unit,
    onResized: (Float, Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var isResizing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset(
                x = cellSize * box.position.x,
                y = cellSize * box.position.y
            )
            .size(
                width = cellSize * box.size.w,
                height = cellSize * box.size.h
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Determine if starting from corner (resize) or center (move)
                        val boxWidth = box.size.w * cellSizePx
                        val boxHeight = box.size.h * cellSizePx
                        isResizing = offset.x > boxWidth - 48 && offset.y > boxHeight - 48
                        isDragging = true
                    },
                    onDragEnd = {
                        isDragging = false
                        isResizing = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (isResizing) {
                            onResized(dragAmount.x, dragAmount.y)
                        } else {
                            onMoved(dragAmount.x, dragAmount.y)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) {
                            onSelected()
                        }
                    }
                }
            }
    ) {
        BoxContent(box = box, isSelected = isSelected)
    }
}

@Composable
private fun BoxItem(
    box: Box,
    cellSize: Dp,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(
                x = cellSize * box.position.x,
                y = cellSize * box.position.y
            )
            .size(
                width = cellSize * box.size.w,
                height = cellSize * box.size.h
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) {
                            onSelected()
                        }
                    }
                }
            }
    ) {
        BoxContent(box = box, isSelected = isSelected)
    }
}