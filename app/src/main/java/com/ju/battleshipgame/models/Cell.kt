package com.ju.battleshipgame.models

import androidx.compose.ui.geometry.Offset

data class Cell(
    val coordinate: Coordinate,
    val wasHit: Boolean
)

data class Coordinate(val col: Char, val row: Int) {

    fun offset(dragOffset: Offset, cellSizePx: Float): Coordinate {
        val gridSize = 10
        val deltaCol = (dragOffset.x / cellSizePx).toInt()
        val deltaRow = (dragOffset.y / cellSizePx).toInt()

        return Coordinate(
            (col + deltaCol).coerceIn('A', 'A' + gridSize - 1),
            (row + deltaRow).coerceIn(1, gridSize)
        )
    }
}