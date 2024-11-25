package com.ju.battleshipgame.models

import androidx.compose.ui.geometry.Offset

data class DragState(
    val ship: Ship? = null,
    val isPlaced: Boolean = false,
    val dragOffset: Offset = Offset.Zero,
    val potentialCells: List<Coordinate> = emptyList()
) {
    fun reset() = copy(ship = null, isPlaced = false, dragOffset = Offset.Zero, potentialCells = emptyList())
}