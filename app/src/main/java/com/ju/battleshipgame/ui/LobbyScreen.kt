import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.R
import com.ju.battleshipgame.models.DEFAULT_PLAYER_SHIPS
import com.ju.battleshipgame.models.Game
import com.ju.battleshipgame.models.GamePlayer
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameViewModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    // Hämta spelarens namn
    val playerName = model.getLocalPlayerName()

    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            if (game.players.any { it.playerId == model.localPlayerId.value }) {
                if (game.gameState == "GAME_IN_PROGRESS") {
                    navController.navigate("setup/$gameId")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.lobby),
                contentScale = ContentScale.Crop
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titelruta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Battleships - $playerName",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista över spelare
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(players.entries.toList()) { (documentId, player) ->
                        if (documentId != model.localPlayerId.value) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Player: ${player.name}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Status: Waiting...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.LightGray
                                        )
                                        var hasGame = false
                                        games.forEach { (gameId, game) ->
                                            val isInvited = game.players.any { it.playerId == model.localPlayerId.value } &&
                                                    game.players.any { it.playerId == documentId } &&
                                                    game.gameState == "invite"

                                            if (isInvited) {
                                                hasGame = true
                                                if (game.players.first().playerId == model.localPlayerId.value) {
                                                    Text("Waiting for response...")
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconButton(onClick = {
                                                            model.db.collection("games").document(gameId)
                                                                .update(
                                                                    "gameState", "GAME_IN_PROGRESS",
                                                                    "currentPlayerId", model.localPlayerId.value
                                                                )
                                                                .addOnSuccessListener {
                                                                    navController.navigate("setup/$gameId")
                                                                }
                                                                .addOnFailureListener {
                                                                    Log.e("LobbyScreen", "Error updating game: $gameId")
                                                                }
                                                        }) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Accept Invite",
                                                                tint = Color.Green
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        IconButton(onClick = {
                                                            model.db.collection("games").document(gameId)
                                                                .delete()
                                                                .addOnSuccessListener {
                                                                    Log.d("LobbyScreen", "Game declined: $gameId")
                                                                }
                                                                .addOnFailureListener {
                                                                    Log.e("LobbyScreen", "Error declining game: ${it.message}")
                                                                }
                                                        }) {
                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Decline Invite",
                                                                tint = Color.Red
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (!hasGame) {
                                            Button(onClick = {
                                                model.db.collection("games")
                                                    .add(
                                                        Game(
                                                            gameState = "invite",
                                                            players = listOf(
                                                                GamePlayer(
                                                                    playerId = model.localPlayerId.value!!,
                                                                    player = players[model.localPlayerId.value]!!,
                                                                    playerShips = DEFAULT_PLAYER_SHIPS,
                                                                    isReady = false
                                                                ),
                                                                GamePlayer(
                                                                    playerId = documentId,
                                                                    player = player,
                                                                    playerShips = DEFAULT_PLAYER_SHIPS,
                                                                    isReady = false
                                                                )
                                                            ),
                                                            // todo add current player
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
