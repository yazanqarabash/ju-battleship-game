package com.ju.battleshipgame.ui.setup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.R
import com.ju.battleshipgame.calculateOccupyingShipOffset
import com.ju.battleshipgame.calculatePotentialCells
import com.ju.battleshipgame.calculateShipPlacement
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.DragState
import com.ju.battleshipgame.models.GameState
import com.ju.battleshipgame.models.Ship
import com.ju.battleshipgame.updateShipIfValid
import kotlinx.coroutines.flow.asStateFlow


private const val GRID_SIZE = 10
private val CELL_SIZE_DP = 32.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    navController: NavController,
    gameId: String?,
    model: GameViewModel
) {
    var isReadyPressed by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var isWaiting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    // Error handling if the gameId is invalid or game not found
    if (gameId == null || !games.containsKey(gameId)) {
        navController.navigate("lobby")
        Toast.makeText(context, "The other player left the game ", Toast.LENGTH_SHORT).show()
        return
    }

    val game = games[gameId]!!
    val gamePlayer = game.players.find { it.playerId == model.localPlayerId.value }
    val opponent = game.players.find { it.playerId != model.localPlayerId.value }

    // Handle error if player or opponent is missing
    if (gamePlayer == null || opponent == null) {
        Log.e("SetupError", "Error: Player not found!")
        Text("Error: Player not found!")
        Spacer(Modifier.height(12.dp))
        IconButton(onClick = { navController.navigate("lobby") }) {
            Icon(Icons.Filled.Clear, contentDescription = "Leave")
        }
        return
    }

    LaunchedEffect(gamePlayer.isReady, opponent.isReady) {
        // Only navigate to the game screen once both players are ready and the game is not already in progress
        if (gamePlayer.isReady && opponent.isReady ) {
            // Update game state to in-progress only once
            model.updateGameState(gameId, GameState.GAME_IN_PROGRESS)
            model.updateCurrentPlayer(gameId, game.players.first().playerId)

            // Wait for the state to update before navigating
            navController.navigate("game/$gameId")
        }
    }
    var ships by remember { mutableStateOf(gamePlayer.playerShips) }

    // Ship movement and placement handling
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
        if (!gamePlayer.isReady) {
            isReadyPressed = true
            isWaiting = true
            gamePlayer.playerShips = ships
            gamePlayer.isReady = true
            model.updatePlayerReadyState(gameId, gamePlayer.playerId, ships, true)
        }
    }

    val onLeaveGame: () -> Unit = {
        Log.d("SetupScreen", "Leave button clicked")
        showLeaveDialog = true // Show dialog when Leave button is clicked
    }

    // Scaffold with background image and layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Board setup") },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray) // Optional fallback color in case of image loading issue
        ) {
            // Add the background image
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Crop or fit the image
            )

            // Content layout
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.7f)) // Slight opacity to contrast text
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Arrange Your Ships",
                    fontSize = 25.sp,
                    color = Color.Black // Adjust text color for better contrast with background
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.size(CELL_SIZE_DP * GRID_SIZE)) {
                    val dragMutableState = remember { mutableStateOf(DragState()) }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GRID_SIZE),
                        modifier = Modifier.matchParentSize()
                    ) {
                        itemsIndexed((1..GRID_SIZE).flatMap { row ->
                            (0 until GRID_SIZE).map { colIndex ->
                                val col = ('A'.code + colIndex).toChar().toString()
                                Coordinate(col, row)
                            }
                        }) { _, coordinate ->
                            val occupyingShip = ships.find { ship ->
                                ship.cells.any { it.coordinate == coordinate }
                            }
                            GridCell(
                                occupyingShip,
                                coordinate,
                                onShipMoved,
                                onShipClicked,
                                dragMutableState
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Ready"-knapp och statusmeddelande
                Button(onClick = onReady, enabled = !isReadyPressed) {
                    Text(text = if (isWaiting) "Waiting for another player..." else "Ready")
                }
                Spacer(modifier = Modifier.height(4.dp))

                Button(onClick = onLeaveGame) {
                    Text(text = "Leave game")
                }

                when (game.gameState) {
                    GameState.CANCELED.toString() -> Text("Game has been canceled. Returning to lobby.")
                    GameState.FINISHED.toString() -> Text("Game finished.")
                }
            }

            // Leave confirmation dialog
            if (showLeaveDialog) {
                AlertDialog(
                    onDismissRequest = { showLeaveDialog = false },
                    title = { Text("Are you sure you want to leave the game?") },
                    text = { Text("Both players will be returned to the lobby and the game will be deleted.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                model.localPlayerId.value?.let {
                                    model.removePlayerAndCheckGameDeletion(
                                        gameId,
                                        it,
                                        navController = navController
                                    )
                                }
                                showLeaveDialog = false
                                navController.navigate("lobby") {
                                    popUpTo("game/$gameId") { inclusive = true }
                                }
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showLeaveDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

 @Composable
fun GridCell(
    occupyingShip: Ship?,
    coordinate: Coordinate,
    onShipMoved: (Ship, Coordinate) -> Unit,
    onShipClicked: (Ship) -> Unit,
    dragMutableState: MutableState<DragState>
) {
    var dragState by dragMutableState
    val cellSizePx = with(LocalDensity.current) { CELL_SIZE_DP.toPx() }
    val isHighlighted = dragState.potentialCells.contains(coordinate)

    Box(
        modifier = Modifier
            .size(CELL_SIZE_DP)
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
                    onDragStart = { touchPosition ->
                        if (occupyingShip != null) {
                            val initialOffset = calculateOccupyingShipOffset(occupyingShip, coordinate.offset(touchPosition, cellSizePx), cellSizePx)

                            dragState = dragState.copy(
                                ship = occupyingShip,
                                dragOffset = initialOffset,
                                potentialCells = calculatePotentialCells(
                                    occupyingShip,
                                    coordinate,
                                    GRID_SIZE
                                )
                            )
                        }
                    },
                    onDragEnd = {
                        if (dragState.ship != null) {
                            val deltaCol = (dragState.dragOffset.x / cellSizePx).toInt()
                            val deltaRow = (dragState.dragOffset.y / cellSizePx).toInt()
                            val newCoordinate = Coordinate(
                                col = ((coordinate.col[0] + deltaCol).coerceIn('A', 'A' + GRID_SIZE - 1)).toChar().toString(),
                                row = (coordinate.row + deltaRow).coerceIn(1, GRID_SIZE)
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
                            GRID_SIZE
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