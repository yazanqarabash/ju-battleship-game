package com.ju.battleshipgame

import LobbyScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ju.battleshipgame.models.Player
import com.ju.battleshipgame.ui.GameScreen
import com.ju.battleshipgame.ui.home.NewPlayerScreen
import com.ju.battleshipgame.ui.setup.SetupScreen

@Composable
fun BattleshipGame() {
    val navController = rememberNavController()
    val model = GameViewModel()
    var currentPlayer = Player( name = "")

    model.initGame()

    // TODO remove hardcoded routes

    NavHost(
        navController = navController,
        startDestination = "newPlayer"
    ) {
        composable("newPlayer") {
            NewPlayerScreen(
                navController = navController,
                model=model,
                onPlayerCreated = { name ->
                    currentPlayer = Player(name = name);
                    navController.navigate("lobby")
                }
            )
        }

        composable("lobby") {
            LobbyScreen(
                navController = navController,
                model=model
                )
        }

        composable("boardSetup/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            SetupScreen(
                navController = navController,
                gameId = gameId ?: "", // ÄNDRING
                model = model
            )
        }

        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, gameId ?: "", model) // ÄNDRING
        }

    }
}