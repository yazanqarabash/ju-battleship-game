package com.ju.battleshipgame

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.ju.battleshipgame.models.Board
import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.ChallengeState
import com.ju.battleshipgame.models.Coordinate
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
            playerBoard = Board(size = 10),
            playerShips = listOf(
                Ship(
                    ShipType.CARRIER, 4,
                    cells = calculateShipPlacement(Coordinate('A', 1), 4, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                ),
                Ship(
                    ShipType.CRUISER, 3,
                    cells = calculateShipPlacement(Coordinate('C', 3), 3, Orientation.VERTICAL)!!,
                    orientation = Orientation.VERTICAL
                ),
                Ship(
                    ShipType.BATTLESHIP, 2,
                    cells = calculateShipPlacement(Coordinate('E', 5), 2, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                ),
                Ship(
                    ShipType.BATTLESHIP, 2,
                    cells = calculateShipPlacement(Coordinate('H', 2), 2, Orientation.VERTICAL)!!,
                    orientation = Orientation.VERTICAL
                ),
                Ship(
                    ShipType.SUBMARINE, 1,
                    cells = calculateShipPlacement(Coordinate('J', 8), 1, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                ),
                Ship(
                    ShipType.DESTROYER, 1,
                    cells = calculateShipPlacement(Coordinate('F', 7), 1, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                )
            ),
            isReady = false
        ),
        GamePLayer(
            player = mockPlayers["player2"]!!,
            playerBoard = Board(size = 10),
            playerShips = listOf(
                Ship(
                    ShipType.CARRIER, 4,
                    cells = calculateShipPlacement(Coordinate('A', 1), 4, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                ),
                Ship(
                    ShipType.CRUISER, 3,
                    cells = calculateShipPlacement(Coordinate('C', 3), 3, Orientation.VERTICAL)!!,
                    orientation = Orientation.VERTICAL
                ),
                Ship(
                    ShipType.BATTLESHIP, 2,
                    cells = calculateShipPlacement(Coordinate('E', 5), 2, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                ),
                Ship(
                    ShipType.BATTLESHIP, 2,
                    cells = calculateShipPlacement(Coordinate('H', 2), 2, Orientation.VERTICAL)!!,
                    orientation = Orientation.VERTICAL
                ),
                Ship(
                    ShipType.SUBMARINE, 1,
                    cells = calculateShipPlacement(Coordinate('J', 8), 1, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                ),
                Ship(
                    ShipType.DESTROYER, 1,
                    cells = calculateShipPlacement(Coordinate('F', 7), 1, Orientation.HORIZONTAL)!!,
                    orientation = Orientation.HORIZONTAL
                )
            ),
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
