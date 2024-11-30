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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.models.Game
import com.ju.battleshipgame.models.GamePlayer
import kotlinx.coroutines.flow.asStateFlow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameViewModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            // Här kollar vi om spelaren är en av spelarna i spelet och om spelet är pågående
            if (game.players.any { it.playerId == model.localPlayerId.value }) {
                if (game.gameState == "GAME_IN_PROGRESS") {
                    // När spelet är i "GAME_IN_PROGRESS", navigera till setup-skärmen för båda spelarna
                    navController.navigate("setup/$gameId")
                }
            }
        }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
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
                            val isInvited = game.players.any { it.playerId == model.localPlayerId.value } &&
                                    game.players.any { it.playerId == documentId } &&
                                    game.gameState == "invite"

                            if (isInvited) {
                                if (game.players.first().playerId == model.localPlayerId.value) {
                                    Text("Waiting for accept...")
                                } else {
                                    Button(onClick = {
                                        model.db.collection("games").document(gameId)
                                            .update(
                                                "gameState", "GAME_IN_PROGRESS",
                                                "currentPlayerId", model.localPlayerId.value
                                            )
                                            .addOnSuccessListener {
                                                // När utmaningen accepteras, navigera både till SetupScreen för den som accepterar och utmanaren.
                                                navController.navigate("setup/$gameId")
                                            }
                                            .addOnFailureListener {
                                                Log.e("LobbyScreen", "Error updating game: $gameId")
                                            }
                                    }) {
                                        Text("Accept invite")
                                    }
                                }
                                hasGame = true
                            }
                        }
                        if (!hasGame) {
                            Button(onClick = {
                                model.db.collection("games")
                                    .add(
                                        Game(
                                            gameState = "invite",
                                            players = listOf(
                                                GamePlayer(playerId = model.localPlayerId.value!!, player = players[model.localPlayerId.value]!!),
                                                GamePlayer(playerId = documentId, player = player)
                                            ),
                                            currentPlayerId = ""
                                        )
                                    )
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
