package com.ju.battleshipgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ju.battleshipgame.models.Player
import com.ju.battleshipgame.ui.lobby.LobbyScreen
import com.ju.battleshipgame.ui.home.Homescreen
import com.ju.battleshipgame.ui.theme.BattleshipGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BattleshipGameTheme {
                val navController = rememberNavController()
                val players = mutableListOf<Player>(
                    Player(id = "1", name = "Player One", invitation = "Pending"),
                    Player(id = "2", name = "Player Two", invitation = "Sent")
                )
                Scaffold(modifier = Modifier.fillMaxSize()){ innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "HomeScreen"
                    ) {
                        composable("HomeScreen") {
                            Homescreen(
                                list = players,
                                navcontroller = navController,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        composable("LobbyScreen") {
                            LobbyScreen(
                                players = players,
                                navController = navController,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

