package com.ju.battleshipgame.models

data class Invite(
    val playerId: String = "",
    var challengeState: ChallengeState = ChallengeState.IDLE
)

enum class ChallengeState {
    IDLE,
    ACCEPT,
    DECLINE,
    WAITING
}
