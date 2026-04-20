package com.dashboard.builder.ui.components.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
    cellSize: Dp = 40.dp,
    availableWidth: Dp = 0.dp,
    isMoveMode: Boolean = false,
    onBoxSelected: (String?) -> Unit,
    onBoxDoubleSelected: (String) -> Unit,
    onBoxMoved: (String, Int, Int) -> Unit,
    onBoxResized: (String, Int, Int) -> Unit,
    onInputChange: (String, String) -> Unit = { _, _ -> },
    onCheckboxToggle: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val density = LocalDensity.current
    
    // Calculate cell size based on available width - fit 10 columns to screen exactly
    val actualCellSize = if (availableWidth.value > 0) {
        availableWidth / GridEngine.COLUMNS
    } else {
        cellSize
    }
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
                    cellSize = actualCellSize,
                    cellSizePx = cellSizePx,
                    isSelected = box.id == selectedBoxId,
                    isMoveMode = isMoveMode,
                    onSelected = { onBoxSelected(box.id) },
                    onDoubleSelected = { onBoxDoubleSelected(box.id) },
                    onMoved = { dxPx, dyPx ->
                        // Convert pixel delta to cell delta
                        val newX = box.position.x + (dxPx / cellSizePx).toInt()
                        val newY = box.position.y + (dyPx / cellSizePx).toInt()
                        onBoxMoved(box.id, newX, newY)
                    },
                    onResized = { dwPx, dhPx ->
                        val newW = box.size.w + (dwPx / cellSizePx).toInt()
                        val newH = box.size.h + (dhPx / cellSizePx).toInt()
                        onBoxResized(box.id, newW, newH)
                    },
                    onInputChange = { onInputChange(box.id, it) },
                    onCheckboxToggle = { onCheckboxToggle(box.id, it) }
                )
            }

            // Render floating boxes (on top)
            tab.boxes.filter { it.floating }.forEach { box ->
                BoxItem(
                    box = box,
                    cellSize = actualCellSize,
                    isSelected = box.id == selectedBoxId,
                    isMoveMode = isMoveMode,
                    onSelected = { onBoxSelected(box.id) },
                    onDoubleSelected = { onBoxDoubleSelected(box.id) },
                    onInputChange = { onInputChange(box.id, it) },
                    onCheckboxToggle = { onCheckboxToggle(box.id, it) }
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
    onDoubleSelected: () -> Unit,
    onMoved: (Float, Float) -> Unit,
    onResized: (Float, Float) -> Unit,
    onInputChange: (String) -> Unit = {},
    onCheckboxToggle: (Int) -> Unit = {}
) {
    // Use rememberUpdatedState to avoid stale closures
    val cellSizePxState by rememberUpdatedState(cellSizePx)
    val onMovedState by rememberUpdatedState(onMoved)
    val onResizedState by rememberUpdatedState(onResized)
    val onSelectedState by rememberUpdatedState(onSelected)
    val onDoubleSelectedState by rememberUpdatedState(onDoubleSelected)
    
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

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
            .pointerInput(isMoveMode) {
                detectTapGestures(
                    onTap = {
                        // In move mode, tap always selects the box (no double-tap edit)
                        if (isMoveMode) {
                            onSelectedState()
                        } else {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastTapTime < 300) {
                                onDoubleSelectedState()
                            } else {
                                onSelectedState()
                            }
                            lastTapTime = currentTime
                        }
                    }
                )
            }
            .then(
                // In move mode, allow dragging any box (even locked). Disable resize in move mode.
                if (isMoveMode || !box.locked) {
                    Modifier.pointerInput(isMoveMode) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // In move mode, select the box when drag starts
                                if (isMoveMode) {
                                    onSelectedState()
                                }
                                totalDragX = 0f
                                totalDragY = 0f
                            },
                            onDragEnd = {
                                totalDragX = 0f
                                totalDragY = 0f
                            },
                            onDragCancel = {
                                totalDragX = 0f
                                totalDragY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount.x
                                totalDragY += dragAmount.y
                                
                                // Disable resize in move mode
                                if (!isMoveMode) {
                                    val boxWidth = box.size.w * cellSizePxState
                                    val boxHeight = box.size.h * cellSizePxState
                                    val isResizing = change.position.x > boxWidth - 48 && 
                                                    change.position.y > boxHeight - 48
                                    
                                    if (isResizing) {
                                        // Resize: only move when drag exceeds half a cell
                                        if (kotlin.math.abs(totalDragX) >= cellSizePxState / 2f) {
                                            onResizedState(
                                                if (totalDragX > 0) cellSizePxState else -cellSizePxState,
                                                0f
                                            )
                                            totalDragX = 0f
                                        }
                                        if (kotlin.math.abs(totalDragY) >= cellSizePxState / 2f) {
                                            onResizedState(
                                                0f,
                                                if (totalDragY > 0) cellSizePxState else -cellSizePxState
                                            )
                                            totalDragY = 0f
                                        }
                                        return@detectDragGestures
                                    }
                                }
                                
                                // Move: only move when drag exceeds half a cell - track axes independently
                                if (kotlin.math.abs(totalDragX) >= cellSizePxState / 2f) {
                                    onMovedState(
                                        if (totalDragX > 0) cellSizePxState else -cellSizePxState,
                                        0f
                                    )
                                    totalDragX = 0f
                                }
                                if (kotlin.math.abs(totalDragY) >= cellSizePxState / 2f) {
                                    onMovedState(
                                        0f,
                                        if (totalDragY > 0) cellSizePxState else -cellSizePxState
                                    )
                                    totalDragY = 0f
                                }
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        BoxContent(
            box = box,
            isSelected = isSelected,
            isMoveMode = isMoveMode,
            onInputChange = onInputChange,
            onCheckboxToggle = onCheckboxToggle
        )
    }
}

@Composable
private fun BoxItem(
    box: Box,
    cellSize: Dp,
    isSelected: Boolean,
    isMoveMode: Boolean = false,
    onSelected: () -> Unit,
    onDoubleSelected: () -> Unit,
    onInputChange: (String) -> Unit = {},
    onCheckboxToggle: (Int) -> Unit = {}
) {
    var lastTapTime by remember { mutableLongStateOf(0L) }
    val onSelectedState by rememberUpdatedState(onSelected)
    val onDoubleSelectedState by rememberUpdatedState(onDoubleSelected)
    
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
                detectTapGestures(
                    onTap = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 300) {
                            onDoubleSelectedState()
                        } else {
                            onSelectedState()
                        }
                        lastTapTime = currentTime
                    }
                )
            }
    ) {
        BoxContent(
            box = box,
            isSelected = isSelected,
            isMoveMode = isMoveMode,
            onInputChange = onInputChange,
            onCheckboxToggle = onCheckboxToggle
        )
    }
}