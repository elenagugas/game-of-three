package com.gugas.takeaway

import com.gugas.takeaway.game.*
import com.gugas.takeaway.service.GameService
import com.gugas.takeaway.service.OtherPlayerWebClient
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient


class GameControllerTest {

    private val otherPlayerRestClient: OtherPlayerWebClient = mockk()
    private val gameService = GameService(otherPlayerRestClient)
    private val gameController = GameController(gameService)
    private val webTestClient = WebTestClient.bindToController(gameController).build()

    @BeforeEach
    fun setUp() {
        coEvery { otherPlayerRestClient.isOtherPlayerAlive() } returns true
        coEvery { otherPlayerRestClient.sendGameMoveToOtherPlayer(any()) } returns mockk()
        Game.restoreDefault()
    }

    @Test
    fun testHealth() {
        webTestClient.get().uri("/game/health").exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.IM_HERE)
    }

    @Test
    fun setGameModeAuto() {
        Game.mode = GameMode.MANUAL
        webTestClient.post().uri("/game/set-auto").exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.CHANGED_MODE)
        Assertions.assertThat(Game.mode).isEqualTo(GameMode.AUTO)
    }

    @Test
    fun setGameModeManual() {
        Game.mode = GameMode.AUTO
        webTestClient.post().uri("/game/set-manual").exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.CHANGED_MODE)
        Assertions.assertThat(Game.mode).isEqualTo(GameMode.MANUAL)
    }

    @Test
    fun startManualGame() {
        webTestClient.post().uri("/game/start/35").exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.STARTED_NEW_GAME)

        Assertions.assertThat(Game.currentNumber).isEqualTo(35)
        Assertions.assertThat(Game.mode).isEqualTo(GameMode.MANUAL)
        Assertions.assertThat(Game.state).isEqualTo(GameState.AWAITING_OTHER_PLAYER_MOVE)
    }

    @Test
    fun startManualGameWhenTheGameIsInProgress() {
        webTestClient.post().uri("/game/start/35").exchange()
        webTestClient.post().uri("/game/start/auto").exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
            .expectBody(String::class.java).isEqualTo(GameResponse.ALREADY_PLAYING)
    }

    @Test
    @Throws(Exception::class)
    fun startManualGameWithInvalidNumberTest() {
        webTestClient.post().uri("/game/start/-1").exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(String::class.java).isEqualTo(GameResponse.INVALID_NUMBER)
    }

    @Test
    fun startAutoGame() {
        webTestClient.post().uri("/game/start/auto").exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.STARTED_NEW_GAME)

        Assertions.assertThat(Game.mode).isEqualTo(GameMode.AUTO)
        Assertions.assertThat(Game.state).isEqualTo(GameState.AWAITING_OTHER_PLAYER_MOVE)
    }


    @Test
    fun startAutoGameWhenTheGameIsInProgress() {
        webTestClient.post().uri("/game/start/auto").exchange()
        webTestClient.post().uri("/game/start/auto").exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
            .expectBody(String::class.java).isEqualTo(GameResponse.ALREADY_PLAYING)
    }

    @Test
    fun yourTurn() {
        webTestClient.post().uri("/game/your-turn").bodyValue(GameMove(MoveType.ZERO, 13)).exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.MADE_A_MOVE)
    }

    @Test
    fun makeMyMoveWhenGameIsNotStarted() {
        webTestClient.post().uri("/game/make-my-move").exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
            .expectBody(String::class.java).isEqualTo(GameResponse.GAME_IS_NOT_STARTED)
    }

    @Test
    fun makeMyMoveWhenAwaitingOtherPlayerMove() {
        webTestClient.post().uri("/game/start/13").exchange()
        webTestClient.post().uri("/game/make-my-move").exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
            .expectBody(String::class.java).isEqualTo(GameResponse.AWAITING_OTHER_PLAYER_MOVE)
    }

    @Test
    fun makeMyMoveWhenItIsMyTurn() {
        webTestClient.post().uri("/game/start/7").exchange()
        webTestClient.post().uri("/game/your-turn").bodyValue(GameMove(MoveType.MINUS_ONE, 2)).exchange()
        webTestClient.post().uri("/game/make-my-move").exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.MADE_A_MOVE)
    }

    @Test
    fun loseGame() {
        webTestClient.post().uri("/game/start/4").exchange()
        webTestClient.post().uri("/game/your-turn").bodyValue(GameMove(MoveType.MINUS_ONE, 1)).exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.CONGRATULATIONS)
    }

    @Test
    fun winGame() {
        webTestClient.post().uri("/game/start/6").exchange()
        webTestClient.post().uri("/game/your-turn").bodyValue(GameMove(MoveType.ZERO, 2)).exchange()
        webTestClient.post().uri("/game/make-my-move").exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(String::class.java).isEqualTo(GameResponse.MADE_A_MOVE)
    }
}