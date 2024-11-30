package com.ju.battleshipgame.ui

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.models.GameState
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun GameScreen(
    navController: NavController,
    gameId: String?,
    model: GameViewModel
) {

    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()
    val game = games[gameId]

    if (gameId == null || game == null) {
        Log.e("GameScreen", "Error: Game not found: $gameId")
        Text("Error: Game not found: $gameId")
        Spacer(Modifier.height(12.dp))
        IconButton(onClick = { navController.navigate("lobby") }) {
            Icon(Icons.Filled.Clear, contentDescription = "Leave")
        }
        return
    }

    // Hitta spelaren och motståndaren baserat på lokal spelare
    val gamePlayer = game.players.find { it.playerId == model.localPlayerId.value }
    val opponent = game.players.find { it.playerId != model.localPlayerId.value }

    // Om spelaren eller motståndaren inte finns
    if (gamePlayer == null || opponent == null) {
        Log.e("GameScreen", "Error: Player not found!")
        Text("Error: Player not found!")
        Spacer(Modifier.height(12.dp))
        IconButton(onClick = { navController.navigate("lobby") }) {
            Icon(Icons.Filled.Clear, contentDescription = "Leave")
        }
        return
    }


    // Hantera spelets tillstånd och spelarnas beredskap
    LaunchedEffect(gamePlayer.isReady, opponent.isReady, game.gameState) {
        // Om båda spelarna är redo, starta spelet
        if (gamePlayer.isReady && opponent.isReady) {
            model.updateGameState(gameId, GameState.GAME_IN_PROGRESS)
            model.updateCurrentPlayer(gameId, gamePlayer.player.name)
            navController.navigate("game/$gameId")
        }
        // Om spelet är avbrutet, gå tillbaka till lobby
        if (game.gameState == GameState.CANCELED.toString()) {
            navController.navigate("lobby")
        }
    }
}
