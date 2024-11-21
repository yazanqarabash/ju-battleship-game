package com.ju.battleshipgame.models

data class Cell(
    val coordinate: Coordinate,
    val wasHit: Boolean
)

data class Coordinate(val col: Char, val row: Int)