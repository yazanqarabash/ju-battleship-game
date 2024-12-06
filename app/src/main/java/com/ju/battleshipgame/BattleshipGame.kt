package com.ju.battleshipgame

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ju.battleshipgame.ui.GameScreen
import com.ju.battleshipgame.ui.LobbyScreen
import com.ju.battleshipgame.ui.NewPlayerScreen
import com.ju.battleshipgame.ui.SetupScreen

@Composable
fun BattleshipGame() {
    val navController = rememberNavController()
    val model = GameViewModel()
    model.initGame()

    NavHost(
        navController = navController,
        startDestination = "player"
    ) {
        composable("player") {
            NewPlayerScreen(navController, model)
        }
        composable("lobby") {
            LobbyScreen(navController, model)
        }
        composable("setup/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            SetupScreen(navController, gameId, model)
        }
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, gameId, model)
        }
    }
}