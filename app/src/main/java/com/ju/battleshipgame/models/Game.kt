package com.ju.battleshipgame.models

data class Game(
    var players: List<GamePlayer> = emptyList(), // Byter från player1Id och player2Id till en lista av spelare.
    var currentPlayerId: String = "",           // Identifierar den aktuella spelarens ID.
    var winner: Player? = null,                 // Vinnare om spelet är avslutat.
    var gameState: String = GameState.SETTING_SHIPS.toString()
)

data class GamePlayer(
    val playerId: String = "",                  // Varje spelare har ett unikt ID.
    val player: Player = Player(),
    var playerShips: List<Ship> = emptyList(),
    var isReady: Boolean = false,
    var shotsFired: List<Coordinate> = emptyList(),  // jag
    var hits: List<Coordinate> = emptyList() // jag
)


enum class GameState {
    SETTING_SHIPS,
    GAME_IN_PROGRESS,
    FINISHED,
    CANCELED,
    INVITE
}
