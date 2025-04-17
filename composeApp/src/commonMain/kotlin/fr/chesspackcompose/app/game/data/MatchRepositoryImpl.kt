package fr.chesspackcompose.app.game.data

import fr.chesspackcompose.app.core.logger.Logger
import fr.chesspackcompose.app.core.network.WebSocketClient
import fr.chesspackcompose.app.game.domain.GameMove
import fr.chesspackcompose.app.game.domain.MatchRepository
import fr.chesspackcompose.app.match_making.domain.Match
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.request.parameter
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

class MatchRepositoryImpl(
    private val client: WebSocketClient
) : MatchRepository {
    private val _opponentMoves = MutableSharedFlow<GameMove>()
    override val opponentMoves: Flow<GameMove> get() = _opponentMoves.asSharedFlow()
    private lateinit var session: DefaultClientWebSocketSession
    override suspend fun startGame(match: Match) {
        client.start(
            path = "/match/${match.matchMaking.gameId}",
            request = { parameter("player", match.playerId) }
        ) {
            session = this
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val json = frame.readText()
                        val event = Json.decodeFromString<GameMove>(json)
                        handleEvent(match = match, move = event)
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                Logger.e("onClose ${closeReason.await()}")
            } catch (e: Exception) {
                Logger.e("onError ${closeReason.await()}")
            }
        }
    }

    override suspend fun move(move: GameMove) {
        session.sendSerialized(move)
    }

    override suspend fun stop() {
        session.close()
    }

    private suspend fun handleEvent(match: Match, move: GameMove) {
        when (move) {
            is GameMove.Move -> {
                if (move.playerId == match.playerId) return
                Logger.d("handleEvent $move")
                _opponentMoves.emit(move)
            }
        }
    }
}