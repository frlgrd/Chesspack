package fr.chesspackcompose.app.match_making.presentation

import fr.chesspackcompose.app.match_making.domain.Match

data class MatchMakingUiState(
    val text: String = "Find game",
    val buttonEnabled: Boolean = true,
    val match: Match? = null,
    val playerId: String = ""
)