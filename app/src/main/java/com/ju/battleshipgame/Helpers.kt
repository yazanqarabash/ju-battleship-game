package com.ju.battleshipgame

import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.Orientation
import com.ju.battleshipgame.models.Ship

fun calculateShipPlacement(
    start: Coordinate,
    length: Int,
    orientation: Orientation
): List<Cell>? {
    val gridSize = 10
    val cells = mutableListOf<Cell>()

    for (i in 0 until length) {
        val cell = when (orientation) {
            Orientation.HORIZONTAL -> {
                if (start.col + i > 'A' + gridSize - 1) return null
                Cell(Coordinate((start.col + i), start.row), false)
            }
            Orientation.VERTICAL -> {
                if (start.row + i > gridSize) return null
                Cell(Coordinate(start.col, start.row + i), false)
            }
        }
        cells.add(cell)
    }

    return cells
}


fun isOverlapping(newCells: List<Cell>, ships: List<Ship>, currentShip: Ship): Boolean {
    return ships.any { ship ->
        ship != currentShip && ship.cells.any { it.coordinate in newCells.map { cell -> cell.coordinate } }
    }
}