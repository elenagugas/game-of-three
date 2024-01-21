package com.gugas.takeaway

import com.gugas.takeaway.game.GameMessages
import com.gugas.takeaway.game.GameState
import org.springframework.http.HttpStatus

data class CheckResult(
    val isAllowed: Boolean,
    val httpCode: HttpStatus = HttpStatus.OK,
    val reason: String = ""
)

object StateChecker {

    fun isYourTurnAllowed(gameState: GameState) =
        if (gameState == GameState.AWAITING_MY_MOVE) {
            CheckResult(false, HttpStatus.FORBIDDEN, GameMessages.AWAITING_MY_MOVE)
        } else {
            CheckResult(true)
        }

    fun isMyTurnAllowed(gameState: GameState) =
        if (gameState == GameState.NOT_INITIALIZED) {
            CheckResult(false, HttpStatus.FORBIDDEN, GameMessages.WE_ARE_NOT_PLAYING_YET)
        } else if (gameState == GameState.AWAITING_OTHER_PLAYER_MOVE) {
            CheckResult(false, HttpStatus.FORBIDDEN, GameMessages.AWAITING_OTHER_PLAYER_MOVE)
        } else {
            CheckResult(true)
        }

    fun isStartGameAllowed(gameState: GameState, number: Int) =
        if (gameState != GameState.NOT_INITIALIZED) {
            CheckResult(false, HttpStatus.FORBIDDEN, GameMessages.WE_ARE_ALREADY_PLAYING)
        } else if (!isInitialNumberAllowed(number)) {
            CheckResult(false, HttpStatus.BAD_REQUEST, GameMessages.INVALID_NUMBER)
        } else {
            CheckResult(true)
        }

    private fun isInitialNumberAllowed(number: Int): Boolean {
        return number > 1;
    }
}