package com.ju.battleshipgame.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ju.battleshipgame.R
import com.ju.battleshipgame.models.Player

@Composable
fun LobbyScreen(
    players: MutableList<Player>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ListView(players = players, navController = navController, modifier = modifier.fillMaxSize())
}

@Composable
fun ListView(players: MutableList<Player>, navController: NavController, modifier: Modifier) {
    Box( modifier=Modifier.fillMaxSize()){
        Image(
            painter = painterResource(id= R.drawable.battleship_lobby),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            items(players) { player ->
                RowView(player = player, navController = navController)
                Spacer(modifier = Modifier.height(13.dp))
                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
            }
        }
    }
        Button(onClick = {
            navController.popBackStack()
        },modifier=Modifier.fillMaxWidth(0.5f).padding(16.dp).align( Alignment.BottomCenter)
        ) {
            Text("Change Name")

        }
    }
}

@Composable
fun RowView(player: Player, navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp)
        ) {
            Text(
                text = player.name,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 8.dp),
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
            )
            Button(
                onClick = {},
                modifier = Modifier
                    .size(width = 100.dp, height = 40.dp)
                    .align(Alignment.CenterEnd)

            ) {
                Text("Challenge")
            }
        }

    }
}
