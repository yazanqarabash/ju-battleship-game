/*
package com.ju.battleshipgame.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.provider.FontsContractCompat.Columns
import androidx.navigation.NavHostController
import com.ju.battleshipgame.R
import com.ju.battleshipgame.models.Player
import com.ju.battleshipgame.ui.theme.BattleshipGameTheme
@Composable
fun Homescreen(
    list: MutableList<Player>,
    navcontroller: NavHostController,
    modifier: Modifier = Modifier
) {
    var playerName by remember { mutableStateOf(" ") }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id= R.drawable.battleship_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)

            )
            Button(
                onClick = {
                    val newPlayer = Player(id = list.size.toString(), name = playerName)
                    list.add(newPlayer)
                    navcontroller.navigate("LobbyScreen")
                },
                enabled = playerName.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join the Lobby")
            }
        }
    }
}
*/
