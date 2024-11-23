package com.ju.battleshipgame.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.calculateShipPlacement
import com.ju.battleshipgame.isOverlapping
import com.ju.battleshipgame.models.Board
import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.GameState
import com.ju.battleshipgame.models.Orientation
import com.ju.battleshipgame.models.Ship
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

private data class DragState(
    val ship: Ship? = null,
    val isPlaced: Boolean = false,
    val dragOffset: Offset = Offset.Zero
) {
    fun reset() = copy(ship = null, isPlaced = false, dragOffset = Offset.Zero)
}


@Composable
fun LazyBoardGrid(
    ships: List<Ship>,
    onShipMoved: (Ship, Coordinate) -> Unit,
    onShipClicked: (Ship) -> Unit
) {
    val gridSize = 10
    val cellSizeDp = 32.dp


    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = Modifier.size(cellSizeDp * gridSize)
    ) {
        itemsIndexed((1..gridSize).flatMap { row -> ('A' until 'A' + gridSize).map { col -> Coordinate(col, row) } }) { _, coordinate ->
            val occupyingShip = ships.find { ship -> ship.cells.any { it.coordinate == coordinate } }

            CellItem(cellSizeDp, occupyingShip, coordinate, gridSize, onShipMoved, onShipClicked)

            // old manual way
/*            Box(
                modifier = Modifier
                    .size(cellSizeDp)
                    .background(if (occupyingShip != null) Color.Yellow else Color.White)
                    .border(1.dp, Color.Black)
                    .offset {
                        if (occupyingShip == dragState.ship) {
                            IntOffset(
                                dragState.dragOffset.x.roundToInt(),
                                dragState.dragOffset.y.roundToInt()
                            )
                        } else {
                            IntOffset.Zero
                        }
                    }
                    .pointerInput(occupyingShip) {
                        detectDragGestures(
                            onDragStart = {
                                if (occupyingShip != null) {
                                    dragState = dragState.copy(
                                        ship = occupyingShip,
                                        isPlaced = true,
                                        dragOffset = Offset.Zero
                                    )
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragState = dragState.copy(
                                    dragOffset = dragState.dragOffset + dragAmount
                                )
                            },
                            onDragEnd = {
                                if (dragState.ship != null) {
                                    val deltaCol = (dragState.dragOffset.x / cellSizePx).toInt()
                                    val deltaRow = (dragState.dragOffset.y / cellSizePx).toInt()
                                    val newCoordinate = Coordinate(
                                        (coordinate.col + deltaCol).coerceIn('A', 'A' + gridSize - 1),
                                        (coordinate.row + deltaRow).coerceIn(1, gridSize)
                                    )
                                    onShipMoved(dragState.ship!!, newCoordinate)
                                    dragState = dragState.reset()
                                }
                            },
                            onDragCancel = {
                                dragState = dragState.reset()
                            }
                        )
                    }
                    .clickable(enabled = occupyingShip != null) {
                        if (occupyingShip != null) {
                            onShipClicked(occupyingShip)
                        }
                    }
            )*/
        }
    }
}

@Composable
fun CellItem(
    cellSizeDp: Dp,
    occupyingShip: Ship?,
    coordinate: Coordinate,
    gridSize: Int,
    onShipMoved: (Ship, Coordinate) -> Unit,
    onShipClicked: (Ship) -> Unit
) {
    val cellSizePx = with(LocalDensity.current) { cellSizeDp.toPx() }
    var dragState by remember { mutableStateOf(DragState()) }

    Box(
        modifier = Modifier
            .size(cellSizeDp)
            .background(if (occupyingShip != null) Color.Yellow else Color.White)
            .border(1.dp, Color.Black)
            .offset {
                if (occupyingShip == dragState.ship) {
                    IntOffset(
                        dragState.dragOffset.x.roundToInt(),
                        dragState.dragOffset.y.roundToInt()
                    )
                } else {
                    IntOffset.Zero
                }
            }
            .pointerInput(occupyingShip) {
                detectDragGestures(
                    onDragStart = {
                        if (occupyingShip != null) {
                            dragState = dragState.copy(
                                ship = occupyingShip,
                                isPlaced = true,
                                dragOffset = Offset.Zero
                            )
                        }
                    },
                    onDragEnd = {
                        if (dragState.ship != null) {
                            val deltaCol = (dragState.dragOffset.x / cellSizePx).toInt()
                            val deltaRow = (dragState.dragOffset.y / cellSizePx).toInt()
                            val newCoordinate = Coordinate(
                                (coordinate.col + deltaCol).coerceIn('A', 'A' + gridSize - 1),
                                (coordinate.row + deltaRow).coerceIn(1, gridSize)
                            )
                            onShipMoved(dragState.ship!!, newCoordinate)
                            dragState = dragState.reset()
                        }
                    },
                    onDragCancel = {
                        dragState = dragState.reset()
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragState = dragState.copy(
                        dragOffset = dragState.dragOffset + dragAmount
                    )
                }
            }
            .clickable(enabled = occupyingShip != null) {
                if (occupyingShip != null) {
                    onShipClicked(occupyingShip)
                }
            }
    )
}

// old manual way

/*@Composable
fun BoardGrid(
    ships: List<Ship>,
    onShipMoved: (Ship, Coordinate) -> Unit,
    onShipClicked: (Ship) -> Unit
) {
    val gridSize = 10
    val cellSizePx = with(LocalDensity.current) { 32.dp.toPx() }
    var dragState by remember { mutableStateOf(DragState()) }

    Box(modifier = Modifier.size(32.dp * gridSize)) {
        Column {
            for (row in 1..gridSize) {
                Row {
                    for (col in 'A' until 'A' + gridSize) {
                        val coordinate = Coordinate(col, row)
                        val occupyingShip = ships.find { ship ->
                            ship.cells.any { it.coordinate == coordinate }
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(if (occupyingShip != null) Color.Yellow else Color.White)
                                .border(1.dp, Color.Black)
                                .offset {
                                    if (occupyingShip == dragState.ship) {
                                        IntOffset(
                                            dragState.dragOffset.x.roundToInt(),
                                            dragState.dragOffset.y.roundToInt()
                                        )
                                    } else {
                                        IntOffset(0, 0)
                                    }
                                }
                                .pointerInput(occupyingShip) {
                                    detectDragGestures(
                                        onDragStart = {
                                            if (occupyingShip != null) {
                                                dragState = dragState.copy(
                                                    ship = occupyingShip,
                                                    isPlaced = true,
                                                    dragOffset = Offset.Zero
                                                )
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragState = dragState.copy(
                                                dragOffset = dragState.dragOffset + dragAmount
                                            )
                                        },
                                        onDragEnd = {
                                            if (dragState.ship != null) {
                                                val deltaCol = (dragState.dragOffset.x / cellSizePx).toInt()
                                                val deltaRow = (dragState.dragOffset.y / cellSizePx).toInt()
                                                val newCoordinate = Coordinate(
                                                    (coordinate.col + deltaCol).coerceIn('A', 'A' + gridSize - 1),
                                                    (coordinate.row + deltaRow).coerceIn(1, gridSize)
                                                )
                                                onShipMoved(dragState.ship!!, newCoordinate)
                                                dragState = dragState.reset()
                                            }
                                        },
                                        onDragCancel = {
                                            dragState = dragState.reset()
                                        }
                                    )
                                }
                                .clickable(enabled = occupyingShip != null) {
                                    if (occupyingShip != null) {
                                        onShipClicked(occupyingShip)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSetupScreen(
    navController: NavController,
    gameId: String?,
    model: GameViewModel
) {
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    if (gameId == null || !games.containsKey(gameId)) {
        navController.navigate("lobby")
        return
    }

    val game = games[gameId]!!
    val gamePlayer = game.players.find { it.player.name == model.localPlayerId.value }

    if (gamePlayer == null) {
        Text(text = "Error: Player not found!")
        return
    }

    var ships by remember { mutableStateOf(gamePlayer.playerShips) }

    val onShipMoved: (Ship, Coordinate) -> Unit = { movedShip, newCoordinate ->
        val newCells = calculateShipPlacement(newCoordinate, movedShip.length, movedShip.orientation)
        if (newCells != null && !isOverlapping(newCells, ships, movedShip)) {
            val updatedShip = movedShip.copy(cells = newCells)
            ships = ships.map { if (it == movedShip) updatedShip else it }
        }
    }

    val onShipClicked: (Ship) -> Unit = { clickedShip ->
        val newOrientation = clickedShip.orientation.opposite()
        val newCells = calculateShipPlacement(clickedShip.cells.first().coordinate, clickedShip.length, newOrientation)
        // maybe extract for reusability?
        if (newCells != null && !isOverlapping(newCells, ships, clickedShip)) {
            val updatedShip = clickedShip.copy(orientation = newOrientation, cells = newCells)
            ships = ships.map { if (it == clickedShip) updatedShip else it }
        }
    }

    val onReady: () -> Unit = {
        gamePlayer.isReady = true
        model.updateGameState(gameId, GameState.GAME_IN_PROGRESS)
        navController.navigate("game/$gameId")
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Board setup") })
        },
        bottomBar = {
            BottomAppBar {
                Text("Multiplayer Battleship Game")
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                // maybe leave the game with FAB?
                onClick = { navController.navigate("lobby") }
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Leave")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Arrange Your Ships")

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.size(320.dp)) {
                LazyBoardGrid(
                    ships = ships,
                    onShipMoved = onShipMoved,
                    onShipClicked = onShipClicked
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onReady,
                enabled = ships.all { it.cells.isNotEmpty() }
            ) {
                Text(text = "Ready")
            }
        }

    }
}