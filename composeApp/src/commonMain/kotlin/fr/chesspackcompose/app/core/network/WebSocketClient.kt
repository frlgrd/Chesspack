package fr.chesspackcompose.app.core.network

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

interface WebSocketClient {
    suspend fun start(
        path: String,
        id: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    )
}