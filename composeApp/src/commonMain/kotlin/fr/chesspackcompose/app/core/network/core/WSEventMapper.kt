package fr.chesspackcompose.app.core.network.core

import fr.chesspackcompose.app.core.network.event.WSEvent
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.serialization.WebsocketDeserializeException

class WSEventMapper {
    suspend fun map(session: DefaultClientWebSocketSession): WSEvent? {
        return try {
            session.receiveDeserialized<WSEvent>()
        } catch (e: WebsocketDeserializeException) {
            null
        }
    }
}