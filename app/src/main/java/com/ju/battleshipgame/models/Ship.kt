package com.ju.battleshipgame.models

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
