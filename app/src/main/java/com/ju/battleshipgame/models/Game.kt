package com.ju.battleshipgame.models

data class Game(
    val players: List<GamePLayer>,
    val currentPlayer: String,
    var winner: Player? = null,
    var gameState: GameState,
)

data class GamePLayer(
    val player: Player,
    val playerShips: List<Ship>,
    var isReady: Boolean
)

enum class GameState {
    SETTING_SHIPS,
    WAITING_FOR_PLAYERS,
    GAME_IN_PROGRESS,
    FINISHED,
}