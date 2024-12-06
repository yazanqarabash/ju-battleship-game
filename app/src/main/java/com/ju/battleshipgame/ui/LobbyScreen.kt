package com.ju.battleshipgame.ui

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.ju.battleshipgame.models.Game
import com.ju.battleshipgame.models.GameState
import com.ju.battleshipgame.models.Player
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameViewModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()
    val playerName = model.getLocalPlayerName()

    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            if (game.players.any { it.playerId == model.localPlayerId.value }) {
                when(game.gameState) {
                    GameState.SETTING_SHIPS.toString() -> navController.navigate("setup/$gameId")
                    GameState.GAME_IN_PROGRESS.toString() -> navController.navigate("game/$gameId")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battleships - $playerName") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.lobby),
                    contentScale = ContentScale.Crop
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerList(
                    players = players,
                    model = model,
                    navController = navController,
                    games = games
                )
            }
        }
    }
}

@Composable
fun PlayerList(
    players: Map<String, Player>,
    model: GameViewModel,
    navController: NavController,
    games: Map<String, Game>
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(players.entries.toList(), key = { it.key }) { (documentId, player) ->
            if (documentId != model.localPlayerId.value) {
                PlayerItem(
                    documentId = documentId,
                    player = player,
                    model = model,
                    navController = navController,
                    games = games
                )
            }
        }
    }
}

@Composable
fun PlayerItem(
    documentId: String,
    player: Player,
    model: GameViewModel,
    navController: NavController,
    games: Map<String, Game>
) {
    val localPlayerId = model.localPlayerId.value
    var hasGame = false

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Player: ${player.name}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                games.forEach { (gameId, game) ->
                    val isInvited = game.players.any { it.playerId == localPlayerId } &&
                            game.players.any { it.playerId == documentId } &&
                            game.gameState == GameState.INVITE.toString()
                    if (isInvited) {
                        hasGame = true
                        if (game.players.first().playerId == localPlayerId) {
                            Text("Waiting for response...")
                        } else {
                            GameInviteActions(
                                gameId = gameId,
                                model = model,
                                navController = navController
                            )
                        }
                    }
                }
                if (!hasGame) {
                    Button(onClick = {
                        model.createGame(documentId = documentId)
                    }) {
                        Text("Challenge")
                    }
                }
            }
        }
    }
}

@Composable
fun GameInviteActions(
    gameId: String,
    model: GameViewModel,
    navController: NavController
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            model.startGame(
                gameId = gameId,
                onSuccess = {
                    navController.navigate("setup/$gameId")
                }
            )
        }) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Accept Invite",
                tint = Color.Green
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = { model.deleteGame(gameId) }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Decline Invite",
                tint = Color.Red
            )
        }
    }
}