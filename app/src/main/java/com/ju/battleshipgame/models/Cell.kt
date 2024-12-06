package com.ju.battleshipgame.models

import androidx.compose.ui.geometry.Offset

data class Cell(
    val coordinate: Coordinate = Coordinate("A", 1),
    val wasHit: Boolean = false
) {
    fun hit(): Cell {
        return this.copy(wasHit = true)
    }
}


data class Coordinate(val col: String = "A", val row: Int = 1) {

    fun offset(dragOffset: Offset, cellSizePx: Float): Coordinate {
        val gridSize = 10
        val deltaCol = (dragOffset.x / cellSizePx).toInt()
        val deltaRow = (dragOffset.y / cellSizePx).toInt()

        val colChar = col[0]
        val newColChar = (colChar + deltaCol).coerceIn('A', 'A' + gridSize - 1)

        return Coordinate(
            col = newColChar.toString(),
            row = (row + deltaRow).coerceIn(1, gridSize)
        )
    }
}
