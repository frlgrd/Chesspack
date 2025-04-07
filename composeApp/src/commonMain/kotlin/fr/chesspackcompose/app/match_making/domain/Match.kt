package fr.chesspackcompose.app.match_making.domain

import fr.chesspackcompose.app.game.domain.PieceColor
import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val matchMaking: MatchMakingStatus.Done,
    val playerId: String
)

fun Match.getPlayerColor(): PieceColor {
    return when (playerId) {
        matchMaking.player1.id -> matchMaking.player1.color
        matchMaking.player2.id -> matchMaking.player2.color
        else -> throw IllegalArgumentException("PlayerId $playerId p1 ${matchMaking.player1} p2 ${matchMaking.player2}")
    }
}

fun Match.getInitialBoardRotation(): Float {
    return when (getPlayerColor()) {
        PieceColor.Black -> 180F
        PieceColor.White -> 0F
    }
}
