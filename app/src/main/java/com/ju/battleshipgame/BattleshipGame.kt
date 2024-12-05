package com.ju.battleshipgame

import LobbyScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ju.battleshipgame.ui.GameScreen
import com.ju.battleshipgame.ui.home.NewPlayerScreen
import com.ju.battleshipgame.ui.SetupScreen

@Composable
fun BattleshipGame() {
    val navController = rememberNavController()
    val model = GameViewModel()
    model.initGame()

    NavHost(
        navController = navController,
        startDestination = "newPlayer"
    ) {
        composable("newPlayer") {
            NewPlayerScreen(
                navController = navController,
                model=model
            )
        }
        composable("lobby") {
            LobbyScreen(
                navController = navController,
                model=model
            )
        }
        composable("Setup/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            SetupScreen(
                navController = navController,
                gameId = gameId,
                model = model
            )
        }
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, gameId, model)
        }
    }
}