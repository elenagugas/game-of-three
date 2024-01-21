package com.gugas.takeaway

import com.gugas.takeaway.game.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity.post
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(GameController::class)
class GameControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var otherPlayerRestClient: OtherPlayerRestTemplate

    @BeforeEach
    fun setUp() {
        `when`(otherPlayerRestClient.isOtherPlayerAlive()).thenReturn(true)
        `when`(otherPlayerRestClient.sendGameMoveToOtherPlayer(MockitoHelper.any())).thenReturn(true)
        Game.restoreDefault()
    }

    @Test
    fun testHealth() {
        mockMvc.get("/game/health")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun setGameModeAuto() {
        Game.mode = GameMode.MANUAL
        mockMvc.post("/game/set-auto")
            .andExpect {
                status { isOk() }
            }
        Assertions.assertThat(Game.mode).isEqualTo(GameMode.AUTO)
    }

    @Test
    fun setGameModeManual() {
        Game.mode = GameMode.AUTO
        mockMvc.post("/game/set-manual")
            .andExpect {
                status { isOk() }
            }
        Assertions.assertThat(Game.mode).isEqualTo(GameMode.MANUAL)
    }

    @Test
    fun startManualGame() {
        mockMvc.post("/game/start/35")
            .andExpect {
                status { isOk() }
            }
        Assertions.assertThat(Game.currentNumber).isEqualTo(35)
        Assertions.assertThat(Game.mode).isEqualTo(GameMode.MANUAL)
        Assertions.assertThat(Game.state).isEqualTo(GameState.AWAITING_OTHER_PLAYER_MOVE)
    }

    @Test
    fun startManualGameWhenTheGameIsInProgress() {
        mockMvc.post("/game/start/35")
        mockMvc.post("/game/start/auto")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @Throws(Exception::class)
    fun startManualGameWithInvalidNumberTest() {
        mockMvc.post("/game/start/-1")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun startAutoGame() {
        mockMvc.post("/game/start/auto")
            .andExpect {
                status { isOk() }
            }
        Assertions.assertThat(Game.mode).isEqualTo(GameMode.AUTO)
        Assertions.assertThat(Game.state).isEqualTo(GameState.AWAITING_OTHER_PLAYER_MOVE)
    }


    @Test
    fun startAutoGameWhenTheGameIsInProgress() {
        mockMvc.post("/game/start/auto")
        mockMvc.post("/game/start/auto")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun makeMyMove() {
        mockMvc.post("/game/make-my-move") {
            param("number", "12")
        }.andExpect {
            status { isForbidden() }
        }
    }
}