package com.ju.battleshipgame

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ju.battleshipgame.ui.BoardSetupScreen
import com.ju.battleshipgame.ui.LobbyScreen

@Composable
fun TestBoardSetupScreen() {
    val mockViewModel = MockGameViewModel()
    BoardSetupScreen(
        navController = rememberNavController(),
        gameId = "game1",
        model = mockViewModel
    )
}

@Composable
fun BattleshipGame() {
    val navController = rememberNavController()
    val model = GameViewModel()
    model.initGame()


/*    NavHost(
        navController = navController,
        startDestination = "player"
    ) {
        composable("player") { NewPlayerScreen(navController, model) }
        composable("lobby") { LobbyScreen(navController, model) }
        composable("boardSetup/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            BoardSetupScreen(
                navController = navController,
                gameId = gameId,
                model = model
            )
        }
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, model, gameId)
        }
    }*/
}