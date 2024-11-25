package com.ju.battleshipgame

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ju.battleshipgame.ui.GameScreen
import com.ju.battleshipgame.ui.setup.SetupScreen

@Composable
fun BattleshipGame() {
    val navController = rememberNavController()
    val model = GameViewModel()
    model.initGame()

    // TODO remove hardcoded routes

    NavHost(
        navController = navController,
        startDestination = "boardSetup/game1"
    ) {
        //composable("player") { NewPlayerScreen(navController, model) }
        //composable("lobby") { LobbyScreen(navController, model) }
        composable("boardSetup/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            SetupScreen(
                navController = navController,
                gameId = "game1",
                model = model
            )
        }
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, gameId, model)
        }
    }
}