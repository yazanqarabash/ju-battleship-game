package com.ju.battleshipgame

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.ju.battleshipgame.models.Cell
import com.ju.battleshipgame.models.DEFAULT_PLAYER_SHIPS
import com.ju.battleshipgame.models.Game
import com.ju.battleshipgame.models.GamePlayer
import com.ju.battleshipgame.models.GameState
import com.ju.battleshipgame.models.Player
import com.ju.battleshipgame.models.Ship
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

    fun addPlayer(playerName: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val newPlayer = Player(playerName)
        db.collection("players")
            .add(newPlayer)
            .addOnSuccessListener { documentRef -> onSuccess(documentRef.id) }
            .addOnFailureListener { error ->
                Log.e(
                    "NewPlayerScreen",
                    "Error creating player: ${error.message}"
                )
                onFailure()
            }
    }

    fun createGame(documentId: String) {
        val newGame = Game(
            gameState = GameState.INVITE.toString(),
            players = listOf(
                GamePlayer(
                    playerId = localPlayerId.value!!,
                    player = playerMap.value[localPlayerId.value]!!,
                    playerShips = DEFAULT_PLAYER_SHIPS,
                    isReady = false
                ),
                GamePlayer(
                    playerId = documentId,
                    player = playerMap.value[documentId]!!,
                    playerShips = DEFAULT_PLAYER_SHIPS,
                    isReady = false
                )
            ),
            currentPlayerId = ""
        )

        db.collection("games")
            .add(newGame)
            .addOnSuccessListener {
                return@addOnSuccessListener
            }
            .addOnFailureListener { error ->
                Log.e("LobbyScreen", "Error creating game: ${error.message}")
            }
    }

    fun startGame(gameId: String, onSuccess: () -> Unit) {
        db.collection("games").document(gameId)
            .update(
                "gameState", GameState.SETTING_SHIPS.toString(),
                "currentPlayerId", localPlayerId.value
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                Log.e("LobbyScreen", "Error updating game: $gameId")
            }
    }

    fun deleteGame(gameId: String) {
        db.collection("games").document(gameId)
            .delete()
            .addOnSuccessListener {
                return@addOnSuccessListener
            }
            .addOnFailureListener {
                Log.e("LobbyScreen", "Error declining game: ${it.message}")
            }
    }

    fun updateGameState(gameId: String, newState: GameState) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Game::class.java)
                if (game?.gameState != newState.toString()) {
                    db.collection("games").document(gameId)
                        .update("gameState", newState.toString())
                        .addOnSuccessListener {
                            return@addOnSuccessListener
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseUpdate", "Failed to update game state: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFetch", "Failed to fetch game: ${e.message}")
            }
    }

    fun updateCurrentPlayer(gameId: String, currentPlayerId: String) {
        db.collection("games").document(gameId)
            .update("currentPlayerId", currentPlayerId)
            .addOnSuccessListener {
                Log.d("FirebaseUpdate", "Current player updated: $currentPlayerId")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUpdate", "Failed to update current player: ${e.message}")
            }
    }

    fun updatePlayerReadyState(
        gameId: String,
        playerId: String,
        ships: List<Ship>,
        isReady: Boolean
    ) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Game::class.java)
                game?.let {
                    val player = it.players.find { player -> player.playerId == playerId }
                    if (player != null && player.isReady != isReady) {
                        val updatedPlayers = it.players.map { gamePlayer ->
                            if (gamePlayer.playerId == playerId) {
                                gamePlayer.copy(playerShips = ships, isReady = isReady)
                            } else gamePlayer
                        }

                        db.collection("games").document(gameId)
                            .update("players", updatedPlayers)
                            .addOnSuccessListener {
                                Log.d("FirebaseUpdate", "Player state updated: $playerId")
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "FirebaseUpdate",
                                    "Failed to update player state: ${e.message}"
                                )
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFetch", "Failed to fetch game: ${e.message}")
            }
    }

    fun removePlayerAndCheckGameDeletion(
        gameId: String,
        playerName: String?,
        navController: NavController
    ) {
        val game = gameMap.value[gameId] ?: return
        val remainingPlayers = game.players.filter { it.player.name != playerName }
        Log.d("Deleting", "There are " + remainingPlayers.size + " players ")
        Log.d("Deleting", "Trying to delete " + gameId)
        db.collection("games")
            .document(gameId)
            .delete()
            .addOnSuccessListener {
                Log.d("GameViewModel", "Game deleted successfully")
                navController.navigate("lobby") {
                    popUpTo("lobby") { inclusive = true }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GameViewModel", "Error deleting game: ${e.message}")
                e.printStackTrace()
            }
        db.collection("games").document(gameId)
            .update(
                mapOf(
                    "players" to remainingPlayers,
                    "gameState" to GameState.CANCELED.toString(),
                    "message" to "$playerName has left the game."
                )
            )
            .addOnSuccessListener {
                Log.d("GameViewModel", "Player removed and game updated successfully")
                navController.navigate("lobby") {
                    popUpTo("lobby") { inclusive = true }
                }
            }
            .addOnFailureListener { e ->

                Log.e("GameViewModel", "Error updating game: ${e.message}")
                e.printStackTrace()
            }

    }

    fun getLocalPlayerName(): String {
        val localId = localPlayerId.value
        return if (localId != null) {
            playerMap.value[localId]?.name ?: "Unknown Player"
        } else {
            "Unknown Player"
        }
    }

    fun makeMove(gameId: String, playerId: String, targetCell: Cell) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Game::class.java)
                if (game != null) {
                    // Kontrollera om det är spelarens tur
                    if (game.currentPlayerId == playerId) {
                        val opponent = game.players.find { it.playerId != playerId }
                        opponent?.let {
                            val hit = it.playerShips.any { ship ->
                                ship.cells.any { cell -> cell.coordinate == targetCell.coordinate }
                            }

                            val updates = mutableMapOf<String, Any>()

                            // Uppdatera träff eller miss
                            if (hit) {
                                // Om det är en träff, markera cellen som träffad
                                val updatedOpponentShips = it.playerShips.map { ship ->
                                    val updatedCells = ship.cells.map { cell ->
                                        if (cell.coordinate == targetCell.coordinate) {
                                            cell.hit() // Markera cellen som träffad
                                        } else {
                                            cell
                                        }
                                    }
                                    val isSunk = updatedCells.all { cell -> cell.wasHit }
                                    ship.copy(cells = updatedCells, isSunk = isSunk)
                                }

                                // Uppdatera spelarens hits
                                val updatedHits = it.hits + targetCell.coordinate

                                // Uppdatera den motståndande spelarens data
                                it.playerShips = updatedOpponentShips
                                it.hits = updatedHits

                                // Kontrollera om motståndaren har förlorat alla skepp
                                if (it.playerShips.all { ship -> ship.cells.all { it.wasHit } }) {
                                    // Om alla skepp är förstörda, sätt vinnaren
                                    val winner = game.players.find { p -> p.playerId == playerId }
                                    winner?.let {
                                        updates["winner"] = it
                                    }
                                    updates["gameState"] = GameState.FINISHED.toString()
                                } else {
                                    updates["gameState"] = GameState.GAME_IN_PROGRESS.toString()
                                }

                                // Behåll spelarens tur
                                updates["currentPlayerId"] = playerId
                            } else {
                                // Om det är en miss, lägg till det i skjutna koordinater och byt tur
                                val updatedShotsFired = it.shotsFired + targetCell.coordinate
                                it.shotsFired = updatedShotsFired

                                // Byt tur till nästa spelare
                                val nextPlayerId = game.players.find { p -> p.playerId != playerId }?.playerId
                                updates["currentPlayerId"] = nextPlayerId ?: playerId
                            }

                            // Uppdatera spelarlistan och spelets status
                            updates["players"] = game.players

                            // Spara uppdateringarna i Firebase
                            db.collection("games").document(gameId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d("GameViewModel", "Move processed.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("GameViewModel", "Failed to update game state: ${e.message}")
                                }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GameViewModel", "Failed to fetch game: ${e.message}")
            }
    }
}