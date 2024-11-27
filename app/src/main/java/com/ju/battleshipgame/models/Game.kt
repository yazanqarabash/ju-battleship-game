package com.ju.battleshipgame.models

data class Game(
    val players: List<GamePLayer> = emptyList(),
    var currentPlayer: String = "",
    var player1Id: String = "",
    var player2Id: String = "",
    var winner: Player? = null,
    var gameState: String = GameState.SETTING_SHIPS.toString(),
)

data class GamePLayer(
    val player: Player = Player(),
    var playerShips: List<Ship> = emptyList(),
    var isReady: Boolean = false
)

enum class GameState {
    SETTING_SHIPS,
    GAME_IN_PROGRESS,
    FINISHED,
    CANCELED,
    Invite
}
