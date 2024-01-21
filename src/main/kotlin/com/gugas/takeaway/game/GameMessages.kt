package com.gugas.takeaway.game

object GameMessages {
    const val IM_HERE = "I'm here"
    const val AWAITING_OTHER_PLAYER_MOVE = "Awaiting other player's move"
    const val AWAITING_MY_MOVE = "Awaiting my move"
    const val WE_ARE_ALREADY_PLAYING = "Game is already in progress, can't start a new game"
    const val WE_ARE_NOT_PLAYING_YET = "We are not playing yet"
    const val STARTED_NEW_GAME = "Started new game"
    const val MADE_A_MOVE = "Made a move"
    const val OTHER_PLAYER_NOT_AVAILABLE = "Second player is unavailable"
    const val INVALID_NUMBER = "Please enter a valid initial number (an integer greater than 1) to start the game"
    const val CONGRATULATIONS = "Congratulations! You won"
    const val LOSER = "Sorry! You lost"
}