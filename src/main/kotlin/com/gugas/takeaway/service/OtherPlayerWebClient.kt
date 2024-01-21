package com.gugas.takeaway.service

import com.gugas.takeaway.game.GameMove
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import kotlin.jvm.optionals.getOrDefault

@Component
class OtherPlayerWebClient(
    @Value("\${other.player.url}") private val otherPlayerUrl: String,
    @Value("\${other.player.endpoint.health}") private val healthEndpoint: String,
    @Value("\${other.player.endpoint.your-turn}") private val yourTurnEndpoint: String
) {

    private val webClient = WebClient.builder()
        .baseUrl(otherPlayerUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    suspend fun isOtherPlayerAlive(): Boolean {
        val response = webClient
            .get()
            .uri(healthEndpoint)
            .retrieve()
            .toEntity(Void::class.java)
            .map { responseEntity -> responseEntity.statusCode == HttpStatus.OK }

        return response.blockOptional().getOrDefault(false)
    }

    fun sendGameMoveToOtherPlayer(gameMove: GameMove) {
        webClient
            .post()
            .uri(yourTurnEndpoint)
            .bodyValue(gameMove)
            .retrieve()
            .toEntity(Void::class.java)
            .subscribe()
    }

}