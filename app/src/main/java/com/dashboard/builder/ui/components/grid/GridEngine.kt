package com.dashboard.builder.ui.components.grid

import com.dashboard.builder.data.model.Box
import com.dashboard.builder.data.model.Position
import com.dashboard.builder.data.model.Size
import com.dashboard.builder.data.model.BoxType
import com.dashboard.builder.data.model.TextConfig

object GridEngine {
    const val COLUMNS = 10
    const val ROWS = 32

    /**
     * Build occupancy grid - returns 2D array where each cell contains boxId or null
     */
    fun buildGrid(boxes: List<Box>): Array<Array<String?>> {
        val grid = Array(ROWS) { Array(COLUMNS) { null as String? } }

        boxes.filter { !it.floating }.forEach { box ->
            for (x in box.position.x until box.position.x + box.size.w) {
                for (y in box.position.y until box.position.y + box.size.h) {
                    if (x in 0 until COLUMNS && y in 0 until ROWS) {
                        grid[y][x] = box.id
                    }
                }
            }
        }

        return grid
    }

    /**
     * Check if a box can be placed at given position (considering all existing boxes except the box being placed)
     */
    fun canPlace(box: Box, newX: Int, newY: Int, allBoxes: List<Box>): Boolean {
        // Bounds check
        if (newX < 0 || newY < 0 || newX + box.size.w > COLUMNS || newY + box.size.h > ROWS) {
            return false
        }

        // Build grid excluding the current box (if it already exists)
        val existingBoxes = allBoxes.filter { it.id != box.id && !it.floating }
        val grid = buildGrid(existingBoxes)

        // Check for overlap with any box (including unlocked ones)
        for (x in newX until newX + box.size.w) {
            for (y in newY until newY + box.size.h) {
                if (x in 0 until COLUMNS && y in 0 until ROWS) {
                    val occupant = grid[y][x]
                    if (occupant != null) {
                        // There's a box already there
                        return false
                    }
                }
            }
        }

        return true
    }

    /**
     * Find the first available position for a box
     */
    fun findFirstAvailable(boxes: List<Box>, size: Size): Position {
        // Start from top-left, find first completely empty spot
        for (y in 0 until ROWS - size.h + 1) {
            for (x in 0 until COLUMNS - size.w + 1) {
                val tempBox = Box(
                    id = "temp",
                    type = BoxType.TEXT,
                    position = Position(x, y),
                    size = size,
                    config = TextConfig()
                )
                if (canPlace(tempBox, x, y, boxes)) {
                    return Position(x, y)
                }
            }
        }
        
        // If no spot found in normal position, try to find any spot (allow overlap with non-locked)
        for (y in 0 until ROWS) {
            for (x in 0 until COLUMNS) {
                if (x + size.w <= COLUMNS && y + size.h <= ROWS) {
                    // Check if only locked boxes are in the way
                    val blocked = boxes.filter { !it.floating }.any { box ->
                        !box.locked && isOverlapping(
                            x, y, size.w, size.h,
                            box.position.x, box.position.y, box.size.w, box.size.h
                        )
                    }
                    if (!blocked) {
                        return Position(x, y)
                    }
                }
            }
        }
        
        return Position(0, 0) // Fallback
    }

    /**
     * Check if two boxes overlap
     */
    fun isOverlapping(
        x1: Int, y1: Int, w1: Int, h1: Int,
        x2: Int, y2: Int, w2: Int, h2: Int
    ): Boolean {
        return !(x1 + w1 <= x2 || x1 >= x2 + w2 || y1 + h1 <= y2 || y1 >= y2 + h2)
    }
}