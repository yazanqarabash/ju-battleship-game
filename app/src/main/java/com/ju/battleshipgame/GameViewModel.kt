package com.ju.battleshipgame

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
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
    var localPlayerId = mutableStateOf<String?>("Bob")
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    val inviteMap =  MutableStateFlow<Map<String, Invite>>(emptyMap())


    fun initGame() {
        // Listen for players
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("GameViewModel", "Error fetching players: ${error.message}") // ÄNDRING
                    return@addSnapshotListener
                }
                if (value != null) {
                    try {
                        val updatedMap = value.documents.associate { doc ->
                            doc.id to doc.toObject(Player::class.java)!!
                        }
                        playerMap.value = updatedMap
                        Log.d("GameViewModel", "Fetched players: $updatedMap") // ÄNDRING
                    } catch (e: Exception) {
                        Log.e("GameViewModel", "Error processing player data: ${e.message}") // ÄNDRING
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

    fun updateGameState(gameId: String, newState: GameState) {
        db.collection("games").document(gameId).update("gameState", newState)
    }

    fun updateCurrentPlayer(gameId: String, newState: String) {
        db.collection("games").document(gameId).update("currentPlayer", newState)
    }

    fun updatePlayerReadyState(gameId: String, playerName: String, ships: List<Ship>, isReady: Boolean) {
        val game = gameMap.value[gameId] ?: return
        val updatedPlayers = game.players.map {
            if (it.player.name == playerName) {
                it.copy(playerShips = ships, isReady = isReady)
            } else {
                it
            }
        }

        db.collection("games").document(gameId)
            .update("players", updatedPlayers)
            .addOnSuccessListener {
                Log.d("GameViewModel", "Player readiness updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("GameViewModel", "Error updating readiness: ${e.message}")
            }
    }

    // TODO test if leaving game is working properly
    fun removePlayerAndCheckGameDeletion(gameId: String, playerName: String?) {
        val game = gameMap.value[gameId] ?: return

        val remainingPlayers = game.players.filter { it.player.name != playerName }

        if (remainingPlayers.isEmpty()) {
            db.collection("games").document(gameId)
                .delete()
                .addOnSuccessListener {
                    Log.d("GameViewModel", "Game deleted successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("GameViewModel", "Error deleting game: ${e.message}")
                }
        } else {
            if (game.gameState == GameState.SETTING_SHIPS.toString()) {
                db.collection("games").document(gameId)
                    .update("gameState", GameState.CANCELED)
                    .addOnSuccessListener {
                        Log.d("GameViewModel", "Game marked as canceled")
                    }
                    .addOnFailureListener { e ->
                        Log.e("GameViewModel", "Error updating game state: ${e.message}")
                    }
            }
            db.collection("games").document(gameId)
                .update("players", remainingPlayers)
                .addOnSuccessListener {
                    Log.d("GameViewModel", "Player removed successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("GameViewModel", "Error removing player: ${e.message}")
                }
        }
    }
}