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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import kotlinx.coroutines.flow.asStateFlow

// fast code to test GameScreen

@Composable
fun GameScreen(
    navController: NavController,
    gameId: String?,
    model: GameViewModel
) {
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    val game = games[gameId]!!

    val gamePlayer = game.players.find { it.player.name == model.localPlayerId.value }
    val opponent = game.players.find { it.player.name != model.localPlayerId.value }

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

    if (gamePlayer == null || opponent == null) {
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

    Text("Game state: ${games[gameId]!!.gameState}")
    Spacer(Modifier.height(12.dp))
    Text("Player 1: ${gamePlayer.player.name}")
    Spacer(Modifier.height(12.dp))
    Text("Player 2: ${opponent.player.name}")

}