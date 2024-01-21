package com.gugas.takeaway.game

object Game {
    var state: GameState = GameState.NOT_INITIALIZED
    var mode: GameMode = GameMode.MANUAL
    var currentNumber: Int = 0

    fun restoreDefault() {
        state = GameState.NOT_INITIALIZED
        mode = GameMode.MANUAL
        currentNumber = 0
    }
}