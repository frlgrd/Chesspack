package fr.chesspackcompose.app.core.network

import fr.chesspackcompose.app.core.network.event.WSEvent
import kotlinx.coroutines.flow.Flow

interface WebSocketClient {
    val event: Flow<WSEvent>
    suspend fun start(id: String)
    suspend fun send(id: String, data: Any)
    suspend fun close(id: String)
}