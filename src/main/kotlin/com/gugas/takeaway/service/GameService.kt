package com.gugas.takeaway.service

import com.gugas.takeaway.StateChecker
import com.gugas.takeaway.game.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class GameService(
    private val otherPlayerRestClient: OtherPlayerWebClient
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun setGameModeManual() {
        Game.mode = GameMode.MANUAL
    }

    fun setGameModeAuto() {
        Game.mode = GameMode.AUTO
    }

    fun yourTurn(gameMove: GameMove): ResponseEntity<String> {
        log.info("Received a move of another player: $gameMove")
        val checkResult = StateChecker.isYourTurnAllowed(Game.state)
        if (!checkResult.isAllowed) {
            return ResponseEntity.status(checkResult.httpCode).body(checkResult.reason)
        }
        if (Game.state == GameState.NOT_INITIALIZED) {
            log.info(GameResponse.STARTED_NEW_GAME + " Initial number: ${gameMove.result}")
        }
        Game.currentNumber = gameMove.result
        if (GameHelper.isGameOver(gameMove.result)) {
            log.info(GameResponse.LOSER)
            Game.restoreDefault()
            return ResponseEntity.ok(GameResponse.CONGRATULATIONS)
        }
        when (Game.mode) {
            GameMode.AUTO -> makeAutoMove(gameMove)
            else -> makeManualMove(gameMove)
        }
        return ResponseEntity.ok(GameResponse.MADE_A_MOVE)
    }

    fun makeMyMove(): ResponseEntity<String> {
        val checkResult = StateChecker.isMyTurnAllowed(Game.state)
        if (!checkResult.isAllowed) {
            return ResponseEntity.status(checkResult.httpCode).body(checkResult.reason)
        }
        val myMove = GameHelper.makeMove(Game.currentNumber)
        log.info(GameResponse.MADE_A_MOVE + ": $myMove")
        otherPlayerRestClient.sendGameMoveToOtherPlayer(myMove)
        Game.currentNumber = myMove.result
        Game.state = GameState.AWAITING_OTHER_PLAYER_MOVE
        checkIfIWon(myMove)
        return ResponseEntity.ok(GameResponse.MADE_A_MOVE)
    }

    suspend fun startGame(number: Int, mode: GameMode): ResponseEntity<String> {
        val checkResult = StateChecker.isStartGameAllowed(Game.state, number)
        if (!checkResult.isAllowed) {
            return ResponseEntity.status(checkResult.httpCode).body(checkResult.reason)
        }
        if (!otherPlayerRestClient.isOtherPlayerAlive()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(GameResponse.OTHER_PLAYER_NOT_AVAILABLE)
        }

        Game.currentNumber = number
        Game.mode = mode
        otherPlayerRestClient.sendGameMoveToOtherPlayer(GameMove(MoveType.ZERO, number))
        Game.state = GameState.AWAITING_OTHER_PLAYER_MOVE
        log.info(GameResponse.STARTED_NEW_GAME + " Initial number: $number")
        return ResponseEntity.ok(GameResponse.STARTED_NEW_GAME)
    }

    fun makeAutoMove(gameMove: GameMove) {
        val myMove = GameHelper.makeMove(gameMove.result)
        log.info("Made my move: $myMove")
        otherPlayerRestClient.sendGameMoveToOtherPlayer(myMove)
        Game.state = GameState.AWAITING_OTHER_PLAYER_MOVE
        checkIfIWon(myMove)
    }

    fun makeManualMove(gameMove: GameMove) {
        Game.currentNumber = gameMove.result
        Game.state = GameState.AWAITING_MY_MOVE
        log.info(GameResponse.AWAITING_MY_MOVE)
    }

    fun checkIfIWon(move: GameMove) {
        if (GameHelper.isGameOver(move.result)) {
            log.info(GameResponse.CONGRATULATIONS)
            Game.restoreDefault()
        }
    }
}