package fr.chesspackcompose.app.core.network.core

import fr.chesspackcompose.app.core.network.WebSocketClient
import fr.chesspackcompose.app.core.network.env.Environment
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod

class WebSocketClientImpl(
    private val httpClient: HttpClient,
    private val environment: Environment
) : WebSocketClient {
    override suspend fun start(
        path: String,
        request: HttpRequestBuilder.() -> Unit,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) = httpClient.webSocket(
        method = HttpMethod.Get,
        host = environment.host,
        port = environment.port,
        path = path,
        request = {
            url.protocol = environment.protocol
            request()
        },
        block = block
    )
}