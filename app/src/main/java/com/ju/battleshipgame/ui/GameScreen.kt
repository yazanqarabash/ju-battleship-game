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
import androidx.compose.material3.Button
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

    // Om spelet inte finns eller om gameId är null, navigera tillbaka till lobbyn
    if (gameId == null || !games.containsKey(gameId)) {
        navController.navigate("lobby")
        Toast.makeText(context, "Game not found!", Toast.LENGTH_SHORT).show()
        return
    }

    // Hämta spelet och lokala spelaren
    val game = games[gameId]!!
    val localPlayer = game.players.find { it.playerId == model.localPlayerId.value }
    val opponent = game.players.find { it.playerId != model.localPlayerId.value }

    // Om ingen av spelarna hittas, navigera tillbaka till lobbyn
    if (localPlayer == null || opponent == null) {
        Log.e("GameError", "Player or opponent not found!")
        navController.navigate("lobby")
        return
    }

    // Kolla om den aktuella spelaren är den som har sin tur
    var currentPlayer = game.players.find { it.playerId == game.currentPlayerId }

    // Hämta motståndarens träffar och missade skott
    val opponentHits = opponent.playerShips.flatMap { ship ->
        ship.cells.filter { it.wasHit }.map { it.coordinate }
    }
    val missedShots = opponent.shotsFired

    // Hämta vinnare och förlorare baserat på playerId
    val winner = game.winner
    val loser = game.players.find { it.playerId != winner?.playerId }

    // Om det finns en vinnare, visa en Toast och färga vinnaren och förloraren
    if (winner != null) {
        // If winner is a Player object, use winner.name directly
        Toast.makeText(
            context,
            "Congratulations, ${winner.player.name}, you won!",
            Toast.LENGTH_SHORT
        ).show()

        loser?.let {
            Text(
                "${it.player.name} has lost the game.",
                fontSize = 24.sp,
                color = Color.Red // Färga förlorarens namn röd
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battleships - Your Turn: ${currentPlayer?.player?.name}") },
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
            // Om spelet är slut, visa vinnaren och förloraren
            winner?.let {
                Text(
                    "Congratulations, ${it.player.name}, you won!",
                    fontSize = 24.sp,
                    color = Color.Green
                )

                loser?.let {
                    Text(
                        "${it.player.name} has lost the game.",
                        fontSize = 24.sp,
                        color = Color.Red
                    )
                }

                // Knapp för att gå tillbaka till lobby
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        navController.navigate("lobby") {
                            popUpTo("game") { inclusive = true }
                        }
                    }
                ) {
                    Text("Back to Lobby")
                }

                return@Column
            }

            // Visa motståndarens bräda
            Text("Opponent's Board", fontSize = 20.sp)
            Board(
                gridSize = GRID_SIZE,
                ships = null, // Inga skepp eftersom det är motståndarens bräda
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
                isOpponentBoard = true, // Markera att detta är motståndarens bräda
                hits = opponentHits,
                misses = missedShots
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visa spelarens bräda
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
    val missedCoordinates = remember { mutableStateListOf<Coordinate>() }

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
                    hits.contains(coordinate) -> Color.Red
                    misses.contains(coordinate) -> Color.LightGray
                    else -> Color.White
                }
                else -> when {
                    occupyingShip != null && occupyingShip.cells.any { it.coordinate == coordinate && it.wasHit } -> Color.Red
                    occupyingShip != null -> Color.Blue
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