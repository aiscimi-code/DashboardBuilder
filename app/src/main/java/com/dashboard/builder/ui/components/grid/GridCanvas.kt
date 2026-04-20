package com.dashboard.builder.ui.components.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
    availableWidth: Dp = 0.dp,
    isMoveMode: Boolean = false,
    onBoxSelected: (String?) -> Unit,
    onBoxMoved: (String, Int, Int) -> Unit,
    onBoxResized: (String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val density = LocalDensity.current
    
    // Calculate cell size based on available width - fit 10 columns to screen
    val calculatedCellSize = if (availableWidth.value > 0) {
        (availableWidth / GridEngine.COLUMNS).coerceAtMost(cellSize)
    } else {
        cellSize
    }
    val actualCellSize = calculatedCellSize
    val cellSizePx = with(density) { actualCellSize.toPx() }

    // Calculate grid dimensions
    val gridWidth = GridEngine.COLUMNS * cellSizePx
    val gridHeight = GridEngine.ROWS * cellSizePx

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .horizontalScroll(horizontalScrollState)
            .background(Color(android.graphics.Color.parseColor(tab.backgroundColor)))
    ) {
        Box(
            modifier = Modifier
                .width(actualCellSize * GridEngine.COLUMNS)
                .height(actualCellSize * GridEngine.ROWS)
                .drawBehind {
                    // Draw grid lines
                    val gridColor = Color.Gray.copy(alpha = 0.3f)
                    val borderColor = Color.Gray.copy(alpha = 0.5f)
                    
                    // Vertical lines (10 columns)
                    for (col in 0..GridEngine.COLUMNS) {
                        val x = col * cellSizePx
                        drawLine(
                            color = if (col == 0 || col == GridEngine.COLUMNS) borderColor else gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, gridHeight),
                            strokeWidth = if (col == 0 || col == GridEngine.COLUMNS) 2f else 1f
                        )
                    }
                    
                    // Horizontal lines (32 rows)
                    for (row in 0..GridEngine.ROWS) {
                        val y = row * cellSizePx
                        drawLine(
                            color = if (row == 0 || row == GridEngine.ROWS) borderColor else gridColor,
                            start = Offset(0f, y),
                            end = Offset(gridWidth, y),
                            strokeWidth = if (row == 0 || row == GridEngine.ROWS) 2f else 1f
                        )
                    }
                }
        ) {
            // Render non-floating boxes
            tab.boxes.filter { !it.floating }.forEach { box ->
                DraggableBoxItem(
                    box = box,
                    cellSize = cellSize,
                    cellSizePx = cellSizePx,
                    isSelected = box.id == selectedBoxId,
                    isMoveMode = isMoveMode,
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
    isMoveMode: Boolean = false,
    onSelected: () -> Unit,
    onMoved: (Float, Float) -> Unit,
    onResized: (Float, Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var isResizing by remember { mutableStateOf(false) }
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

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
            .then(
                // In move mode, allow dragging even locked boxes. Otherwise only unlocked.
                if (!box.locked || isMoveMode) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val boxWidth = box.size.w * cellSizePx
                                val boxHeight = box.size.h * cellSizePx
                                isResizing = offset.x > boxWidth - 48 && offset.y > boxHeight - 48
                                totalDragX = 0f
                                totalDragY = 0f
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                isResizing = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount.x
                                totalDragY += dragAmount.y
                                
                                // Only move/resize when drag exceeds half a cell
                                if (isResizing) {
                                    if (kotlin.math.abs(totalDragX) >= cellSizePx / 2f || kotlin.math.abs(totalDragY) >= cellSizePx / 2f) {
                                        onResized(
                                            if (totalDragX > 0) cellSizePx else -cellSizePx,
                                            if (totalDragY > 0) cellSizePx else -cellSizePx
                                        )
                                        totalDragX = 0f
                                        totalDragY = 0f
                                    }
                                } else {
                                    if (kotlin.math.abs(totalDragX) >= cellSizePx / 2f || kotlin.math.abs(totalDragY) >= cellSizePx / 2f) {
                                        onMoved(
                                            if (totalDragX > 0) 1f else -1f,
                                            if (totalDragY > 0) 1f else -1f
                                        )
                                        totalDragX = 0f
                                        totalDragY = 0f
                                    }
                                }
                            }
                        )
                    }
                } else Modifier
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