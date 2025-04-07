package fr.chesspackcompose.app.match_making.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.chesspackcompose.app.match_making.domain.Match
import fr.chesspackcompose.app.match_making.domain.MatchMakingRepository
import fr.chesspackcompose.app.match_making.domain.MatchMakingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MatchMakingViewModel(
    private val matchMakingRepository: MatchMakingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MatchMakingUiState())
    val state = _state.asStateFlow()

    init {
        matchMakingRepository.status.onEach { update ->
            _state.update {
                when (update) {
                    is MatchMakingStatus.Done -> it.copy(
                        match = Match(
                            matchMaking = update,
                            playerId = state.value.playerId
                        )
                    )

                    is MatchMakingStatus.MatchMakingInProgress -> it.copy(
                        buttonEnabled = false,
                        text = "Waiting for opponent ${update.progress}"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: MatchMakingUiEvent) {
        when (event) {
            is MatchMakingUiEvent.Start -> startMatchMaking(event.playerId)
        }
    }

    private fun startMatchMaking(playerId: String) {
        _state.update { it.copy(buttonEnabled = false, playerId = playerId) }
        viewModelScope.launch {
            matchMakingRepository.startMatchMaking(playerId = playerId)
        }
    }
}