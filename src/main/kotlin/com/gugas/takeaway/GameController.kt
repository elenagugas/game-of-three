package com.gugas.takeaway

import com.gugas.takeaway.game.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

enum class GameMode {
    AUTO, MANUAL
}

@RestController
@RequestMapping("/game")
class GameController(
    private val otherPlayerRestClient: OtherPlayerRestTemplate
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/set-auto")
    fun setGameModeAuto(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        Game.mode = GameMode.AUTO
        return ResponseEntity.ok("Game mode was changed successfully")
    }

    @PostMapping("/set-manual")
    fun setGameModeManual(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        Game.mode = GameMode.MANUAL
        return ResponseEntity.ok("Game mode was changed successfully")
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        return ResponseEntity.ok(GameMessages.IM_HERE)
    }

    @PostMapping("/start/{number}")
    fun startManualGame(@PathVariable number: Int): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        return startGame(number, GameMode.MANUAL)
    }

    @PostMapping("/start/auto")
    fun startAutoGame(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        val number = GameHelper.generateRandomInitialNumber()
        return startGame(number, GameMode.AUTO)
    }

    @PostMapping("/your-turn")
    fun yourTurn(@RequestBody gameMove: GameMove): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        log.info("Received a move of another player: $gameMove")
        val checkResult = StateChecker.isYourTurnAllowed(Game.state)
        if (!checkResult.isAllowed) {
            return ResponseEntity.status(checkResult.httpCode).body(checkResult.reason)
        }
        Game.currentNumber = gameMove.result
        if (GameHelper.isGameOver(gameMove.result)) {
            log.info(GameMessages.LOSER)
            Game.restoreDefault()
            return ResponseEntity.ok(GameMessages.CONGRATULATIONS)
        }
        when (Game.mode) {
            GameMode.AUTO -> {
                val myMove = GameHelper.makeMove(gameMove.result)
                log.info("Made my move: $myMove")
                otherPlayerRestClient.sendGameMoveToOtherPlayer(myMove)
                Game.state = GameState.AWAITING_OTHER_PLAYER_MOVE
                haveIWon(myMove)
            }

            else -> {
                Game.currentNumber = gameMove.result
                Game.state = GameState.AWAITING_MY_MOVE
                log.info("Waiting for your move")
            }
        }
        return ResponseEntity.ok(GameMessages.MADE_A_MOVE)
    }

    @PostMapping("/make-my-move")
    fun makeMyMove(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        val checkResult = StateChecker.isMyTurnAllowed(Game.state)
        if (!checkResult.isAllowed) {
            return ResponseEntity.status(checkResult.httpCode).body(checkResult.reason)
        }
        val myMove = GameHelper.makeMove(Game.currentNumber)
        log.info("Made my move: $myMove")
        otherPlayerRestClient.sendGameMoveToOtherPlayer(myMove)
        Game.currentNumber = myMove.result
        Game.state = GameState.AWAITING_OTHER_PLAYER_MOVE
        haveIWon(myMove)
        return ResponseEntity.ok(GameMessages.MADE_A_MOVE)
    }

    private fun startGame(number: Int, mode: GameMode): ResponseEntity<String> {
        val checkResult = StateChecker.isStartGameAllowed(Game.state, number)
        if (!checkResult.isAllowed) {
            return ResponseEntity.status(checkResult.httpCode).body(checkResult.reason)
        }
        if (!otherPlayerRestClient.isOtherPlayerAlive()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(GameMessages.OTHER_PLAYER_NOT_AVAILABLE)
        }

        Game.currentNumber = number
        Game.mode = mode
        otherPlayerRestClient.sendGameMoveToOtherPlayer(GameMove(MoveType.ZERO, number))
        Game.state = GameState.AWAITING_OTHER_PLAYER_MOVE
        log.info("The game was started. Initial number: $number")
        return ResponseEntity.ok(GameMessages.STARTED_NEW_GAME)
    }

    private fun haveIWon(move: GameMove) {
        if (GameHelper.isGameOver(move.result)) {
            log.info(GameMessages.CONGRATULATIONS)
            Game.restoreDefault()
        }
    }
}