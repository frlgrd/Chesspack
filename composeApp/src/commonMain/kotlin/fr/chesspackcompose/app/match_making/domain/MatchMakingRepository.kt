package fr.chesspackcompose.app.match_making.domain

import kotlinx.coroutines.flow.Flow

interface MatchMakingRepository {
    val status: Flow<MatchMakingStatus>
    suspend fun startMatchMaking()
    suspend fun stop()
}