package com.ju.battleshipgame.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.Ship
import kotlinx.coroutines.flow.asStateFlow

private const val GRID_SIZE = 10
private val CELL_SIZE_DP = 32.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    gameId: String?,
    model: GameViewModel
) {
    val context = LocalContext.current
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    if (gameId == null || !games.containsKey(gameId)) {
        navController.navigate("lobby")
        Toast.makeText(context, "Game not found!", Toast.LENGTH_SHORT).show()
        return
    }

    val game = games[gameId]!!
    val localPlayer = game.players.find { it.playerId == model.localPlayerId.value }
    val opponent = game.players.find { it.playerId != model.localPlayerId.value }

    if (localPlayer == null || opponent == null) {
        Log.e("GameError", "Player or opponent not found!")
        navController.navigate("lobby")
        return
    }

    val isCurrentPlayer = game.currentPlayerId == localPlayer.playerId
    val opponentHits = opponent.playerShips.flatMap { ship ->
        ship.cells.filter { it.wasHit }.map { it.coordinate }
    }
    val missedShots = opponent.shotsFired

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battleships - Your Turn: ${if (isCurrentPlayer) "Yes" else "No"}") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Opponent's Board", fontSize = 20.sp)
            Board(
                gridSize = GRID_SIZE,
                ships = null, // Inga skepp eftersom det är motståndarens bräda
                onCellClick = { coordinate ->
                    if (isCurrentPlayer) {
                        val targetCell = Cell(coordinate)

                        if (targetCell.wasHit || coordinate in missedShots) {
                            Toast.makeText(
                                context,
                                "This cell has already been hit!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            model.localPlayerId.value?.let {
                                model.makeMove(gameId, it, targetCell)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Wait for your turn", Toast.LENGTH_SHORT).show()
                    }
                },
                isOpponentBoard = true, // Markera att detta är motståndarens bräda
                hits = opponentHits,
                misses = missedShots
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Your Board", fontSize = 20.sp)
            Board(
                gridSize = GRID_SIZE,
                ships = localPlayer.playerShips, // Dina skepp visas här
                onCellClick = null,
                isOpponentBoard = false // Markera att detta är spelarens bräda
            )
        }
    }
}

@Composable
fun Board(
    gridSize: Int,
    ships: List<Ship>?,
    onCellClick: ((Coordinate) -> Unit)?,
    isOpponentBoard: Boolean,
    hits: List<Coordinate> = emptyList(),
    misses: List<Coordinate> = emptyList()
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = Modifier.size(CELL_SIZE_DP * gridSize)
    ) {
        itemsIndexed((1..gridSize).flatMap { row ->
            (0 until gridSize).map { colIndex ->
                val col = ('A'.code + colIndex).toChar().toString()
                Coordinate(col, row)
            }
        }) { _, coordinate ->

            // Logik för att hitta skepp och status
            val occupyingShip = ships?.find { ship ->
                ship.cells.any { cell -> cell.coordinate == coordinate }
            }

            // Färglogik
            val cellColor = when {
                isOpponentBoard && hits.contains(coordinate) -> Color.Red
                isOpponentBoard && misses.contains(coordinate) -> Color.LightGray
                !isOpponentBoard && occupyingShip != null && occupyingShip.cells.any { it.coordinate == coordinate && it.wasHit } -> Color.Red
                !isOpponentBoard && occupyingShip != null -> Color.Blue
                else -> Color.White
            }

            Box(
                modifier = Modifier
                    .size(CELL_SIZE_DP)
                    .background(cellColor)
                    .border(1.dp, Color.LightGray)
                    .clickable(enabled = onCellClick != null && isOpponentBoard) {
                        onCellClick?.invoke(coordinate)
                    }
            )
        }
    }
}





/*@Composable
fun GameScreen(
    navController: NavController,
    gameId: String?,
    model: GameViewModel
) {

    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()
    val game = games[gameId]

    // Ensure gameId is valid
    if (gameId == null || game == null) {
        Log.e("GameScreen", "Error: Game not found: $gameId")
        Text("Error: Game not found: $gameId")
        Spacer(Modifier.height(12.dp))
        IconButton(onClick = { navController.navigate("lobby") }) {
            Icon(Icons.Filled.Clear, contentDescription = "Leave")
        }
        return
    }

    val gamePlayer = game.players.find { it.playerId == model.localPlayerId.value }
    val opponent = game.players.find { it.playerId != model.localPlayerId.value }

    if (gamePlayer == null || opponent == null) {
        Log.e("GameScreen", "Error: Player not found!")
        Text("Error: Player not found!")
        Spacer(Modifier.height(12.dp))
        IconButton(onClick = { navController.navigate("lobby") }) {
            Icon(Icons.Filled.Clear, contentDescription = "Leave")
        }
        return
    }

    LaunchedEffect(gamePlayer.isReady, opponent.isReady, game.gameState) {
        if (gamePlayer.isReady && opponent.isReady && game.gameState != GameState.GAME_IN_PROGRESS.toString()) {
            model.updateGameState(gameId, GameState.GAME_IN_PROGRESS)
            model.updateCurrentPlayer(gameId, gamePlayer.player.name)
            navController.navigate("game/$gameId")
        }

        // Handle canceled game state
        if (game.gameState == GameState.CANCELED.toString()) {
            navController.navigate("lobby")
        }
    }
}
*/