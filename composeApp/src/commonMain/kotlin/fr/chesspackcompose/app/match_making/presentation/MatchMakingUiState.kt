package fr.chesspackcompose.app.match_making.presentation

import fr.chesspackcompose.app.match_making.domain.MatchMakingStatus

data class MatchMakingUiState(
    val text: String = "Find game",
    val buttonEnabled: Boolean = true,
    val matchMakingStatus: MatchMakingStatus? = null
)