package com.ju.battleshipgame.models

import com.ju.battleshipgame.calculateShipPlacement

val DEFAULT_PLAYER_SHIPS = listOf(
    Ship(
        ShipType.CARRIER, 4,
        cells = calculateShipPlacement(Coordinate('A', 1), 4, Orientation.HORIZONTAL)!!,
        orientation = Orientation.HORIZONTAL
    ),
    Ship(
        ShipType.CRUISER, 3,
        cells = calculateShipPlacement(Coordinate('C', 3), 3, Orientation.VERTICAL)!!,
        orientation = Orientation.VERTICAL
    ),
    Ship(
        ShipType.PATROL_BOAT, 2,
        cells = calculateShipPlacement(Coordinate('E', 5), 2, Orientation.HORIZONTAL)!!,
        orientation = Orientation.HORIZONTAL
    ),
    Ship(
        ShipType.BATTLESHIP, 2,
        cells = calculateShipPlacement(Coordinate('H', 2), 2, Orientation.VERTICAL)!!,
        orientation = Orientation.VERTICAL
    ),
    Ship(
        ShipType.SUBMARINE, 1,
        cells = calculateShipPlacement(Coordinate('J', 8), 1, Orientation.HORIZONTAL)!!,
        orientation = Orientation.HORIZONTAL
    ),
    Ship(
        ShipType.DESTROYER, 1,
        cells = calculateShipPlacement(Coordinate('F', 7), 1, Orientation.HORIZONTAL)!!,
        orientation = Orientation.HORIZONTAL
    )
)

data class Ship(
    val idByShipType: ShipType,
    val length: Int,
    var cells: List<Cell>,
    var orientation: Orientation,
    var isSunk: Boolean = false
)

enum class ShipType {
    CARRIER,
    BATTLESHIP,
    CRUISER,
    DESTROYER,
    SUBMARINE,
    PATROL_BOAT
}

enum class Orientation {
    VERTICAL,
    HORIZONTAL;

    fun opposite() = if (this == VERTICAL) HORIZONTAL else VERTICAL
}
