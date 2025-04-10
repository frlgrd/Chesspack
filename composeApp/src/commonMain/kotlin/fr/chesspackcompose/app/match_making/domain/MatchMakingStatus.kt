package fr.chesspackcompose.app.match_making.domain

import fr.chesspackcompose.app.game.domain.PieceColor
import fr.chesspackcompose.app.match_making.data.MatchMakingSerializer
import kotlinx.serialization.Serializable

@Serializable(with = MatchMakingSerializer::class)
sealed interface MatchMakingStatus {
    @Serializable
    data class MatchMakingInProgress(val progress: Int) : MatchMakingStatus

    @Serializable
    data class Done(
        val player1: MatchPlayer,
        val player2: MatchPlayer,
        val gameId: String
    ) : MatchMakingStatus
}

@Serializable
data class MatchPlayer(
    val id: String,
    val color: PieceColor
)