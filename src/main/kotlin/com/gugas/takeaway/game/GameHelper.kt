package com.gugas.takeaway.game

object GameHelper {

    fun makeMove(number: Int): GameMove {
        val moveType = when (number % 3) {
            1 -> MoveType.MINUS_ONE
            2 -> MoveType.PLUS_ONE
            else -> MoveType.ZERO
        }
        return GameMove(moveType, (number + moveType.value) / 3)
    }

    fun isGameOver(number: Int): Boolean {
        return number == 1
    }

    fun generateRandomInitialNumber() : Int {
        return (0..100).random()
    }
}
