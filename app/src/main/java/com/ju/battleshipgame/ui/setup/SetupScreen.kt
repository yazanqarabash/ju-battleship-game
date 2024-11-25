package com.ju.battleshipgame.ui.setup

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.MockGameViewModel
import com.ju.battleshipgame.calculateShipPlacement
import com.ju.battleshipgame.isOverlapping
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.DragState
import com.ju.battleshipgame.models.GameState
import com.ju.battleshipgame.models.Orientation
import com.ju.battleshipgame.models.Ship
import com.ju.battleshipgame.updateShipIfValid
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    navController: NavController,
    gameId: String?,
    model: MockGameViewModel
) {
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    if (gameId == null || !games.containsKey(gameId)) {
        Log.e(
            "SetupError",
            "Error: Game not found: $gameId"
        )
        Text("Error: Game not found: $gameId")
        Spacer(Modifier.height(12.dp))
        IconButton(
            onClick = { navController.navigate("lobby") }
        ) {
            Icon(Icons.Filled.Clear, contentDescription = "Leave")
        }
        return
    }

    val game = games[gameId]!!
    val gamePlayer = game.players.find { it.player.name == model.localPlayerId.value }

    if (gamePlayer == null) {
        Log.e(
            "SetupError",
            "Error: Player not found!"
        )
        Text(text = "Error: Player not found!")
        Spacer(Modifier.height(12.dp))
        IconButton(
            onClick = { navController.navigate("lobby") }
        ) {
            Icon(Icons.Filled.Clear, contentDescription = "Leave")
        }
        return
    }

    var ships by remember { mutableStateOf(gamePlayer.playerShips) }

    val onShipMoved: (Ship, Coordinate) -> Unit = { movedShip, newCoordinate ->
        val newCells = calculateShipPlacement(newCoordinate, movedShip.length, movedShip.orientation)
        ships = updateShipIfValid(movedShip, newCells, movedShip.orientation, ships)
    }

    val onShipClicked: (Ship) -> Unit = { clickedShip ->
        val newOrientation = clickedShip.orientation.opposite()
        val newCells = calculateShipPlacement(
            clickedShip.cells.first().coordinate,
            clickedShip.length,
            newOrientation
        )
        ships = updateShipIfValid(clickedShip, newCells, newOrientation, ships)
    }

    val onReady: () -> Unit = {

        // TODO perhaps add LaunchedEffect with while loop to update gamestate

        gamePlayer.isReady = true
        model.updateGameState(gameId, GameState.WAITING_FOR_PLAYERS)
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
                val gridSize = 10
                val cellSizeDp = 32.dp
                val dragMutableState = remember { mutableStateOf(DragState()) }

                Box(modifier = Modifier.size(cellSizeDp * gridSize)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridSize),
                        modifier = Modifier.matchParentSize()
                    ) {
                        itemsIndexed((1..gridSize).flatMap { row -> ('A' until 'A' + gridSize).map { col -> Coordinate(col, row) } }) { _, coordinate ->
                            val occupyingShip = ships.find { ship -> ship.cells.any { it.coordinate == coordinate } }

                            GridCell(
                                cellSizeDp,
                                occupyingShip,
                                coordinate,
                                gridSize,
                                onShipMoved,
                                onShipClicked,
                                dragMutableState
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReady
            ) {
                Text(text = "Ready")
            }
        }

    }
}

@Composable
fun GridCell(
    cellSizeDp: Dp,
    occupyingShip: Ship?,
    coordinate: Coordinate,
    gridSize: Int,
    onShipMoved: (Ship, Coordinate) -> Unit,
    onShipClicked: (Ship) -> Unit,
    dragMutableState: MutableState<DragState>
) {
    var dragState by dragMutableState
    val cellSizePx = with(LocalDensity.current) { cellSizeDp.toPx() }
    val isHighlighted = dragState.potentialCells.contains(coordinate)

    fun calculatePotentialCells(
        ship: Ship?,
        draggedCoordinate: Coordinate,
        gridSize: Int
    ): List<Coordinate> {
        if (ship == null) return emptyList()

        val startCol = draggedCoordinate.col
        val startRow = draggedCoordinate.row

        return (0 until ship.length).mapNotNull { offset ->
            val col = if (ship.orientation == Orientation.HORIZONTAL) startCol + offset else startCol
            val row = if (ship.orientation == Orientation.VERTICAL) startRow + offset else startRow

            if (col in 'A' until 'A' + gridSize && row in 1..gridSize) {
                Coordinate(col, row)
            } else {
                null
            }
        }
    }

    Box(
        modifier = Modifier
            .size(cellSizeDp)
            .background(if (occupyingShip != null) Color.Blue else Color.White)
            .border(
                width = if (isHighlighted) 3.dp else 1.dp,
                color = when {
                    isHighlighted && occupyingShip != null && occupyingShip != dragState.ship -> Color.Red
                    isHighlighted -> Color.Green
                    occupyingShip != null -> Color.Blue
                    else -> Color.LightGray
                }
            )
            .pointerInput(occupyingShip) {
                detectDragGestures(
                    onDragStart = {
                        if (occupyingShip != null) {
                            dragState = dragState.copy(
                                ship = occupyingShip,
                                isPlaced = true,
                                dragOffset = Offset.Zero,
                                potentialCells = calculatePotentialCells(
                                    occupyingShip,
                                    coordinate,
                                    gridSize
                                )
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
                        dragOffset = dragState.dragOffset + dragAmount,
                        potentialCells = calculatePotentialCells(
                            dragState.ship,
                            coordinate.offset(dragState.dragOffset + dragAmount, cellSizePx),
                            gridSize
                        )
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