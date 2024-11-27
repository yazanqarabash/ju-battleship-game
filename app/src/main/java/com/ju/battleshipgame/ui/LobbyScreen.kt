import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.models.Game
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameViewModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            if ((game.player1Id == model.localPlayerId.value || game.player2Id == model.localPlayerId.value)
                && game.gameState == "player1_turn") {
                navController.navigate("game/$gameId")
            }
        }
    }
    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let {
        playerName = it.name
    }

    Scaffold(
        topBar = { TopAppBar(title =  { Text("Battelships - $playerName")}) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(players.entries.toList()) { (documentId, player) ->
                if (documentId != model.localPlayerId.value) {
                    ListItem(
                        headlineContent = {
                            Text("Player Name: ${player.name}")
                        },
                        supportingContent = {
                            Text("Status: ...")
                        },
                        trailingContent = {
                            var hasGame = false
                            games.forEach { (gameId, game) ->
                                if (game.player1Id == model.localPlayerId.value && game.player2Id == documentId && game.gameState == "invite") {
                                    Text("Waiting for accept...")
                                    hasGame = true
                                } else if (game.player2Id == model.localPlayerId.value && game.player1Id == documentId && game.gameState == "invite") {
                                    Button(onClick = {
                                        model.db.collection("games").document(gameId)
                                            .update("gameState", "player1_turn")
                                            .addOnSuccessListener {
                                                navController.navigate("game/$gameId")
                                            }
                                            .addOnFailureListener {
                                                Log.e("LobbyScreen", "Error updating game: $gameId")
                                            }
                                    }) {
                                        Text("Accept invite")
                                    }
                                    hasGame = true
                                }
                            }
                            if (!hasGame) {
                                Button(onClick = {
                                    model.db.collection("games")
                                        .add(Game(gameState = "invite", player1Id = model.localPlayerId.value!!, player2Id = documentId))
                                        .addOnSuccessListener { documentRef ->
                                            Log.d("LobbyScreen", "Game created: ${documentRef.id}")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("LobbyScreen", "Error creating game: ${e.message}")
                                        }
                                }) {
                                    Text("Challenge")
                                }
                            }
                        }

                    )
                }
            }
        }
    }
}
