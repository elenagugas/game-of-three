package com.gugas.takeaway

import com.gugas.takeaway.game.GameHelper
import com.gugas.takeaway.game.GameMode
import com.gugas.takeaway.game.GameMove
import com.gugas.takeaway.game.GameResponse
import com.gugas.takeaway.service.GameService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/game")
class GameController(
    private val gameService: GameService
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/health")
    fun health(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        return ResponseEntity.ok(GameResponse.IM_HERE)
    }

    @PostMapping("/set-auto")
    fun setGameModeAuto(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        gameService.setGameModeAuto()
        return ResponseEntity.ok(GameResponse.CHANGED_MODE)
    }

    @PostMapping("/set-manual")
    fun setGameModeManual(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        gameService.setGameModeManual()
        return ResponseEntity.ok(GameResponse.CHANGED_MODE)
    }

    @PostMapping("/start/{number}")
    suspend fun startManualGame(@PathVariable number: Int): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        return gameService.startGame(number, GameMode.MANUAL)
    }

    @PostMapping("/start/auto")
    suspend fun startAutoGame(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        val number = GameHelper.generateRandomInitialNumber()
        return gameService.startGame(number, GameMode.AUTO)
    }

    @PostMapping("/your-turn")
    fun yourTurn(@RequestBody gameMove: GameMove): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        return gameService.yourTurn(gameMove)
    }

    @PostMapping("/make-my-move")
    fun makeMyMove(): ResponseEntity<String> {
        log.info("Endpoint was triggered: " + Thread.currentThread().stackTrace[1].methodName)
        return gameService.makeMyMove()
    }
}
