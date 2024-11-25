package com.ju.battleshipgame

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.ju.battleshipgame.ui.setup.SetupScreen

@Composable
fun TestBoardSetupScreen() {
    val mockViewModel = MockGameViewModel()
    SetupScreen(
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