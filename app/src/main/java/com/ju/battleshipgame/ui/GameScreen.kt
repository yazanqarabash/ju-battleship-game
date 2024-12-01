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
