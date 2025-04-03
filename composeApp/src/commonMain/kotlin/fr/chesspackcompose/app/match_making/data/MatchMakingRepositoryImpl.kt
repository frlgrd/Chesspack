package fr.chesspackcompose.app.match_making.data

import fr.chesspackcompose.app.core.logger.Logger
import fr.chesspackcompose.app.core.network.WebSocketClient
import fr.chesspackcompose.app.match_making.domain.MatchMakingRepository
import fr.chesspackcompose.app.match_making.domain.MatchMakingStatus
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class MatchMakingRepositoryImpl(
    private val client: WebSocketClient
) : MatchMakingRepository {

    private val _status = MutableSharedFlow<MatchMakingStatus>()
    override val status: Flow<MatchMakingStatus> get() = _status.asSharedFlow()
    private lateinit var session: DefaultClientWebSocketSession

    override suspend fun startMatchMaking(playerId: String) {
        client.start(path = "/match-making", id = playerId) {
            session = this
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val event = Json.decodeFromString<MatchMakingStatus>(frame.readText())
                        Logger.d("on match making event $event")
                        _status.emit(event)
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                Logger.e("onClose ${closeReason.await()}")
            } catch (e: Exception) {
                Logger.e("onError ${closeReason.await()}")
            }
        }
    }

    override suspend fun stop() {
        if (!session.isActive) return
        session.close()
    }
}