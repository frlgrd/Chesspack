package fr.chesspackcompose.app.core.network

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.request.HttpRequestBuilder

interface WebSocketClient {
    suspend fun start(
        path: String,
        request: HttpRequestBuilder.() -> Unit = {},
        block: suspend DefaultClientWebSocketSession.() -> Unit
    )
}