package com.ju.battleshipgame

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.ju.battleshipgame.models.Game
import com.ju.battleshipgame.models.GameState
import com.ju.battleshipgame.models.Invite
import com.ju.battleshipgame.models.Player
import com.ju.battleshipgame.models.Ship
import kotlinx.coroutines.flow.MutableStateFlow

open class GameViewModel: ViewModel() {
    val db = Firebase.firestore

    // TODO take localPlayerId from sharedpreferences
    var localPlayerId = mutableStateOf<String?>(null)
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    val inviteMap = MutableStateFlow<Map<String, Invite>>(emptyMap())


    fun initGame() {
        // Listen for players
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("GameViewModel", "Error fetching players: ${error.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    try {
                        val updatedMap = value.documents.associate { doc ->
                            doc.id to doc.toObject(Player::class.java)!!
                        }
                        playerMap.value = updatedMap
                        Log.d("GameViewModel", "Fetched players: $updatedMap")
                    } catch (e: Exception) {
                        Log.e("GameViewModel", "Error processing player data: ${e.message}")
                    }
                }
            }

        // Listen for invite
        db.collection("invites")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Invite::class.java)!!
                    }
                    inviteMap.value = updatedMap
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

    fun updateGameState(gameId: String, gameState: GameState) {
        db.collection("games").document(gameId)
            .update("gameState", gameState.toString())
            .addOnSuccessListener {
                Log.d("FirebaseUpdate", "Game state updated: $gameState")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUpdate", "Failed to update game state: ${e.message}")
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
                    // Find the player and check if the ready state has changed
                    val player = it.players.find { player -> player.playerId == playerId }
                    if (player != null && player.isReady!= isReady) {
                        // Update the player readiness state and ships if it's different
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
                                Log.e("FirebaseUpdate", "Failed to update player state: ${e.message}")
                            }
                    } else {
                        Log.d("FirebaseUpdate", "No changes detected for player $playerId")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFetch", "Failed to fetch game: ${e.message}")
            }
    }



    // TODO test if leaving game is working properly
    fun removePlayerAndCheckGameDeletion(
        gameId: String,
        playerName: String?,
        navController: NavController
    ) {
        val game = gameMap.value[gameId] ?: return
        val remainingPlayers = game.players.filter { it.player.name != playerName }
        Log.d ("Deleting", "There are " + remainingPlayers.size + " players ")
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

}