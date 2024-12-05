package com.ju.battleshipgame.ui.home

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.ju.battleshipgame.GameViewModel
import com.ju.battleshipgame.R
import com.ju.battleshipgame.models.Player

import androidx.compose.ui.platform.LocalContext

@Composable
fun NewPlayerScreen(
    navController: NavHostController,
    model: GameViewModel,
    modifier: Modifier = Modifier
) {
    // todo sharedpreferences
    val context = LocalContext.current
    var playerName by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val playerCollection = db.collection("players")

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.lobby),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
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
                    val newPlayer = Player(name = playerName)
                    playerCollection.add(mapOf("name" to newPlayer.name))
                        .addOnSuccessListener { documentRef ->
                            model.localPlayerId.value = documentRef.id
                            Log.d("NewPlayerScreen", "Player added successfully: $playerName")
                            navController.navigate("lobby")
                        }
                        .addOnFailureListener { e ->
                            Log.e("NewPlayerScreen", "Error adding player: ${e.message}")
                            Toast.makeText(context, "Error adding player. Try again.", Toast.LENGTH_SHORT).show()
                        }
                },
                enabled = playerName.trim().isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join the Lobby")
            }
        }
    }
}
