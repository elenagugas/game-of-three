package com.gugas.takeaway.game

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate


@Component
class OtherPlayerRestTemplate(
    @Value("\${other.player.url.health}") private val otherPlayerUrlHealth: String,
    @Value("\${other.player.url.move}") private val otherPlayerUrlMove: String
)  {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val restTemplate = RestTemplate()

    fun isOtherPlayerAlive(): Boolean {
        return try {
            val response = restTemplate.getForEntity(otherPlayerUrlHealth, String::class.java)
            response.statusCode == HttpStatus.OK
        } catch (ex: Exception) {
            log.error(GameMessages.OTHER_PLAYER_NOT_AVAILABLE, ex)
            false
        }
    }

    fun sendGameMoveToOtherPlayer(gameMove: GameMove): Boolean {
        return try {
            val response = restTemplate.postForEntity(otherPlayerUrlMove, gameMove, String::class.java)
            response.statusCode == HttpStatus.OK
        } catch (ex: Exception) {
            log.error(GameMessages.OTHER_PLAYER_NOT_AVAILABLE, ex)
            false
        }
    }

}