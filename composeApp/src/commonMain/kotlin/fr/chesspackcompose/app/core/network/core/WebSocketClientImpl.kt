package fr.chesspackcompose.app.core.network.core

import fr.chesspackcompose.app.core.logger.Logger
import fr.chesspackcompose.app.core.network.WebSocketClient
import fr.chesspackcompose.app.core.network.event.WSEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.io.IOException

class WebSocketClientImpl(
    private val httpClient: HttpClient,
    private val mapper: WSEventMapper
) : WebSocketClient {

    private val _events = MutableSharedFlow<WSEvent>()
    override val event: Flow<WSEvent> = _events.asSharedFlow().onEach(::println)
    private val sessions = mutableMapOf<String, DefaultClientWebSocketSession>()
    override suspend fun start(id: String) {
        _events.tryEmit(WSEvent.Service.SessionStartRequested)
        val session = try {
            httpClient.webSocketSession("ws://10.0.2.2:8080/tasks")
        } catch (e: IOException) {
            Logger.e("Cannot connect to socket $id", throwable = e)
            return
        }
        sessions[id] = session
        for (frame in session.incoming) {
            onReceive(session = session, frame = frame)
        }
    }

    override suspend fun send(id: String, data: Any) {
        val session = sessions[id] ?: return
        session.sendSerialized(data)
    }

    override suspend fun close(id: String) {
        val session = sessions[id] ?: return
        session.close(reason = CloseReason(CloseReason.Codes.NORMAL, ""))
        sessions.remove(id)
        _events.tryEmit(WSEvent.Service.SessionClosed)
    }

    private suspend fun onReceive(session: DefaultClientWebSocketSession, frame: Frame) {
        Logger.d("frame received ${frame.print()}")
        val event = mapper.map(session) ?: return
        Logger.d("mapped $event}")
        _events.tryEmit(event)
    }

    private fun Frame.print(): String = when (this) {
        is Frame.Text -> readText()
        else -> toString()
    }
}