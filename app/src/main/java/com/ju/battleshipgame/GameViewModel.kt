package com.ju.battleshipgame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.ChallengeState
import com.ju.battleshipgame.models.Coordinate
import com.ju.battleshipgame.models.DEFAULT_PLAYER_SHIPS
import com.ju.battleshipgame.models.Game
import com.ju.battleshipgame.models.GamePLayer
import com.ju.battleshipgame.models.GameState
import com.ju.battleshipgame.models.Orientation
import com.ju.battleshipgame.models.Player
import com.ju.battleshipgame.models.Ship
import com.ju.battleshipgame.models.ShipType
import kotlinx.coroutines.flow.MutableStateFlow

open class GameViewModel: ViewModel() {
    val db = Firebase.firestore
    var localPlayerId = mutableStateOf<String?>(null)
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())

    fun initGame() {
        // Listen for players
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Player::class.java)!!
                    }
                    playerMap.value = updatedMap
                }
            }
        // Listen for games
        db.collection("games")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Game::class.java)!!
                    }
                    gameMap.value = updatedMap
                }
            }
    }

    fun updateGameState(gameId: String?, newState: GameState) {
        if (gameId == null) return
        val game = gameMap.value[gameId] ?: return
        db.collection("games").document(gameId).update("gameState", newState)
    }
}

val mockPlayers = mapOf(
    "player1" to Player(
        name = "Alice",
        challengeState = ChallengeState.ACCEPT,
    ),
    "player2" to Player(
        name = "Bob",
        challengeState = ChallengeState.ACCEPT,
    )
)

val mockGame = Game(
    players = listOf(
        GamePLayer(
            player = mockPlayers["player1"]!!,
            playerShips = DEFAULT_PLAYER_SHIPS,
            isReady = false
        ),
        GamePLayer(
            player = mockPlayers["player2"]!!,
            playerShips = DEFAULT_PLAYER_SHIPS,
            isReady = false
        )
    ),
    currentPlayer = "player1",
    winner = null,
    gameState = GameState.SETTING_SHIPS
)


class MockGameViewModel : GameViewModel() {
    init {
        // Add mock game
        gameMap.value = mapOf("game1" to mockGame)

        // Set local player ID for testing
        localPlayerId.value = "Alice"
    }
}
