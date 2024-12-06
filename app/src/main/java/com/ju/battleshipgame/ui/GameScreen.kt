package com.ju.battleshipgame.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.R
import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.GamePlayer
import com.ju.battleshipgame.models.GameState
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
        Toast.makeText(context, "Game not found!", Toast.LENGTH_SHORT).show()
        navController.navigate("lobby")
        return
    }

    val game = games[gameId]!!
    val localPlayer = game.players.find { it.playerId == model.localPlayerId.value }
    val opponent = game.players.find { it.playerId != model.localPlayerId.value }

    if (localPlayer == null || opponent == null) {
        Toast.makeText(context, "Player or opponent not found!", Toast.LENGTH_SHORT).show()
        navController.navigate("lobby")
        return
    }

    val currentPlayer = game.players.find { it.playerId == game.currentPlayerId }

    val opponentHits = opponent.playerShips.flatMap { ship ->
        ship.cells.filter { it.wasHit }.map { it.coordinate }
    }

    val missedShots = opponent.shotsFired

    val opponentMisses = localPlayer.shotsFired.filterNot { fired ->
        opponent.playerShips.any { ship ->
            ship.cells.any { cell -> cell.coordinate == fired }
        }
    }

    val winner = game.winner
    val loser = game.players.find { it.playerId != winner?.playerId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (game.gameState == GameState.FINISHED.toString()) {
                        Text("Battleships - Game Finished")
                    } else {
                        Text("Battleships - ${currentPlayer?.player?.name}'s Turn")
                    }
                },
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
            winner?.let {
                GameResult(winner = it, loser = loser, navController = navController)
                return@Column
            }
            Text("Opponent's Board", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Board(
                gridSize = GRID_SIZE,
                ships = opponent.playerShips,
                onCellClick = { coordinate ->
                    if (currentPlayer != null) {
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
                isOpponentBoard = true,
                hits = opponentHits,
                misses = missedShots
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Board", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Board(
                gridSize = GRID_SIZE,
                ships = localPlayer.playerShips,
                onCellClick = null,
                isOpponentBoard = false,
                misses = opponentMisses
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

            val occupyingShip = ships?.find { ship ->
                ship.cells.any { cell -> cell.coordinate == coordinate }
            }

            val cellColor = when {
                isOpponentBoard -> when {
                    occupyingShip != null && occupyingShip.isSunk -> Color.Black
                    hits.contains(coordinate) -> Color.Red
                    misses.contains(coordinate) -> Color.LightGray
                    else -> Color.White
                }
                else -> when {
                    occupyingShip != null && occupyingShip.isSunk -> Color.Black
                    occupyingShip != null && occupyingShip.cells.any { it.coordinate == coordinate && it.wasHit } -> Color.Red
                    occupyingShip != null -> Color.Blue
                    misses.contains(coordinate) -> Color.LightGray
                    else -> Color.White
                }
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

@Composable
fun GameResult(
    winner: GamePlayer?,
    loser: GamePlayer?,
    navController: NavController
) {
    if (winner != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = "Winner Trophy",
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🎉 Congratulations! 🎉",
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${winner.player.name} has won the game!",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
            loser?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    imageVector = Icons.Filled.SentimentDissatisfied,
                    contentDescription = "Sad Face",
                    tint = Color.Gray,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${it.player.name} has lost the game.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    navController.navigate("lobby") {
                        popUpTo("game") { inclusive = true }
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text("Back to Lobby", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}