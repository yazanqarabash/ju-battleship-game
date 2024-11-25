package com.ju.battleshipgame

import androidx.compose.ui.geometry.Offset
import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.Orientation
import com.ju.battleshipgame.models.Ship

fun calculateShipPlacement(
    start: Coordinate,
    length: Int,
    orientation: Orientation,
    gridSize: Int = 10
): List<Cell>? {
    val cells = mutableListOf<Cell>()

    for (i in 0 until length) {
        val cell = when (orientation) {
            Orientation.HORIZONTAL -> {
                val colChar = (start.col[0] + i)
                if (colChar > 'A' + gridSize - 1) return null
                Cell(Coordinate(colChar.toString(), start.row), false)
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

fun updateShipIfValid(
    ship: Ship,
    newCells: List<Cell>?,
    newOrientation: Orientation,
    ships: List<Ship>
): List<Ship> {
    return if (newCells != null && !isOverlapping(newCells, ships, ship)) {
        val updatedShip = ship.copy(
            orientation = newOrientation,
            cells = newCells
        )
        ships.map { if (it == ship) updatedShip else it }
    } else {
        ships
    }
}

fun calculatePotentialCells(
    ship: Ship?,
    draggedCoordinate: Coordinate,
    gridSize: Int
): List<Coordinate> {
    if (ship == null) return emptyList()

    return (0 until ship.length).mapNotNull { offset ->
        val colChar = if (ship.orientation == Orientation.HORIZONTAL) {
            (draggedCoordinate.col[0] + offset)
        } else {
            draggedCoordinate.col[0]
        }
        val row = if (ship.orientation == Orientation.VERTICAL) {
            draggedCoordinate.row + offset
        } else {
            draggedCoordinate.row
        }

        if (colChar in 'A'..<'A' + gridSize && row in 1..gridSize) {
            Coordinate(colChar.toString(), row)
        } else {
            null
        }
    }
}

fun calculateOccupyingShipOffset(
    ship: Ship,
    draggedCoordinate: Coordinate,
    cellSizePx: Float
): Offset {
    val cellIndexInShip = when (ship.orientation) {
        Orientation.HORIZONTAL -> draggedCoordinate.col[0] - ship.cells.first().coordinate.col[0]
        Orientation.VERTICAL -> draggedCoordinate.row - ship.cells.first().coordinate.row
    }

    return when (ship.orientation) {
        Orientation.HORIZONTAL -> Offset(-cellIndexInShip * cellSizePx, 0f)
        Orientation.VERTICAL -> Offset(0f, -cellIndexInShip * cellSizePx)
    }
}

fun isOverlapping(newCells: List<Cell>, ships: List<Ship>, currentShip: Ship): Boolean {
    return ships.any { ship ->
        ship != currentShip && ship.cells.any { it.coordinate in newCells.map { cell -> cell.coordinate } }
    }
}