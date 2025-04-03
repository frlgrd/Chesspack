package fr.chesspackcompose.app.core.network.core

import fr.chesspackcompose.app.core.network.WebSocketClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.parameter

class WebSocketClientImpl(
    private val httpClient: HttpClient
) : WebSocketClient {
    override suspend fun start(
        path: String,
        id: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) = httpClient.webSocket(
        host = "10.0.2.2",
        port = 8080,
        path = path,
        request = { parameter("id", id) },
        block = block
    )
}