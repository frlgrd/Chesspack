package fr.chesspackcompose.app.core.network.core

import fr.chesspackcompose.app.core.network.WebSocketClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol

class WebSocketClientImpl(
    private val httpClient: HttpClient
) : WebSocketClient {
    override suspend fun start(
        path: String,
        id: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) = httpClient.webSocket(
        method = HttpMethod.Get,
        host = "chesspack-71354d94eb2f.herokuapp.com",
        port = 443,
        path = path,
        request = {
            url.protocol = URLProtocol.WSS
            parameter("id", id)
        },
        block = block
    )
}