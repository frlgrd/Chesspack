package fr.chesspackcompose.app.match_making.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.chesspackcompose.app.match_making.domain.MatchMakingRepository
import fr.chesspackcompose.app.match_making.domain.MatchMakingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MatchMakingViewModel(
    private val matchMakingRepository: MatchMakingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MatchMakingUiState())
    val state = _state.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    private val playerId = Uuid.random().toString()

    init {
        matchMakingRepository.status.onEach { update ->
            _state.update {
                when (update) {
                    is MatchMakingStatus.MatchMakingDone -> it.copy(
                        text = "Match found",
                        buttonEnabled = false,
                        matchMakingStatus = update
                    )

                    is MatchMakingStatus.MatchMakingInProgress -> it.copy(
                        buttonEnabled = false,
                        text = "Waiting for opponent ${update.progress}",
                        matchMakingStatus = update
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: MatchMakingUiEvent) {
        when (event) {
            MatchMakingUiEvent.Start -> startMatchMaking()
        }
    }

    private fun startMatchMaking() {
        _state.update { it.copy(buttonEnabled = false) }
        viewModelScope.launch {
            matchMakingRepository.startMatchMaking(playerId = playerId)
        }
    }
}