package com.ju.battleshipgame.models

data class Player(
    var name: String = "",
    var challengeState: ChallengeState,
    var isReady: Boolean
)

enum class ChallengeState {
    IDLE,
    ACCEPT,
    DECLINE
}