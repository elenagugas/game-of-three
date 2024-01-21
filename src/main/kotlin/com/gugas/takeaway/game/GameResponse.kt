package com.gugas.takeaway.game

object GameResponse {
    const val IM_HERE = "I'm here"
    const val CHANGED_MODE = "Game mode was changed successfully"
    const val AWAITING_OTHER_PLAYER_MOVE = "Awaiting other player's move"
    const val AWAITING_MY_MOVE = "Awaiting my move"
    const val ALREADY_PLAYING = "Game is already in progress, can't start a new game"
    const val GAME_IS_NOT_STARTED = "We are not playing yet"
    const val STARTED_NEW_GAME = "Started new game"
    const val MADE_A_MOVE = "Made a move"
    const val OTHER_PLAYER_NOT_AVAILABLE = "Second player is unavailable"
    const val INVALID_NUMBER = "Please enter a valid initial number (an integer greater than 1) to start the game"
    const val CONGRATULATIONS = "Congratulations! You won"
    const val LOSER = "Sorry! You lost"
}