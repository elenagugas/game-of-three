package com.gugas.takeaway.game


enum class MoveType(val value: Int) {
    PLUS_ONE(1),
    MINUS_ONE(-1),
    ZERO(0)
}

enum class GameState {
    NOT_INITIALIZED,
    AWAITING_MY_MOVE,
    AWAITING_OTHER_PLAYER_MOVE
}

data class GameMove(
    val move: MoveType,
    val result: Int
)