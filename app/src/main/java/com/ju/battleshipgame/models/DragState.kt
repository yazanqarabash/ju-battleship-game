package com.ju.battleshipgame.models

import androidx.compose.ui.geometry.Offset

data class DragState(
    val ship: Ship? = null,
    val dragOffset: Offset = Offset.Zero,
    val potentialCells: List<Coordinate> = emptyList()
) {
    fun reset() = copy(ship = null, dragOffset = Offset.Zero, potentialCells = emptyList())
}