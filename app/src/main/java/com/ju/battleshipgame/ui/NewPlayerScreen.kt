package com.ju.battleshipgame.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.R

@Composable
fun NewPlayerScreen(
    navController: NavHostController,
    model: GameViewModel
) {
    val context = LocalContext.current
    val sharedPreferences = LocalContext.current.getSharedPreferences("BattleshipPrefs", Context.MODE_PRIVATE)
    var playerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        model.localPlayerId.value = sharedPreferences.getString("playerId", null)
        if (model.localPlayerId.value != null) {
            navController.navigate("lobby")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.lobby),
                contentScale = ContentScale.Crop,
                alpha = 0.7f
            ),
        contentAlignment = Alignment.Center
    ) {
        if (model.localPlayerId.value == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Enter your name") },
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                TextButton(
                    onClick = {
                        model.addPlayer(
                            playerName,
                            onSuccess = { newPlayerId ->
                                sharedPreferences.edit().putString("playerId", newPlayerId)
                                    .apply()
                                model.localPlayerId.value = newPlayerId
                                navController.navigate("lobby")
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Error creating player. Try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    },
                    enabled = playerName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                        .alpha(if(playerName.isNotBlank()) 1f else 0.5f)
                        .background(Color.Blue)
                ) {
                    Text(
                        text = "Join the Lobby",
                        color = Color.White
                    )
                }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}
