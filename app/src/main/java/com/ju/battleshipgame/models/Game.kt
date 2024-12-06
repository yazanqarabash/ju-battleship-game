package com.ju.battleshipgame.models

data class Game(
    var players: List<GamePlayer> = emptyList(),
    var currentPlayerId: String = "",
    var winner: GamePlayer? = null,
    var gameState: String = GameState.SETTING_SHIPS.toString()
)

data class GamePlayer(
    val playerId: String = "",
    val player: Player = Player(),
    var playerShips: List<Ship> = emptyList(),
    var isReady: Boolean = false,
    var shotsFired: List<Coordinate> = emptyList(),
    var hits: List<Coordinate> = emptyList()
)


enum class GameState {
    SETTING_SHIPS,
    GAME_IN_PROGRESS,
    FINISHED,
    CANCELED,
    INVITE
}
