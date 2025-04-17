package fr.chesspackcompose.app.game.domain

import fr.chesspackcompose.app.match_making.domain.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    val opponentMoves: Flow<GameMove>
    suspend fun startGame(match: Match)
    suspend fun move(move: GameMove)
    suspend fun stop()
}