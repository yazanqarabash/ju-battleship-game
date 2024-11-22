package com.ju.battleshipgame.models

data class Player(
    var name: String = "",
    var challengeState: ChallengeState,
)

enum class ChallengeState {
    IDLE,
    ACCEPT,
    DECLINE
}