package fr.chesspackcompose.app.match_making.domain

import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val matchMakingDone: MatchMakingStatus.Done,
    val playerId: String
)
