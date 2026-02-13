package com.example.tasalicool.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tasalicool.models.Game400Engine
import com.example.tasalicool.ui.state.GameUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {

    private val engine = Game400Engine()

    private val _uiState = MutableStateFlow(
        GameUiState(
            phase = engine.phase,
            players = engine.players,
            currentPlayerIndex = engine.currentPlayerIndex,
            currentTrick = engine.currentTrick,
            winner = engine.winner
        )
    )

    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        engine.onGameUpdated = {
            updateUiState()
        }
        engine.startGame()
    }

    private fun updateUiState() {
        _uiState.value = GameUiState(
            phase = engine.phase,
            players = engine.players.toList(),
            currentPlayerIndex = engine.currentPlayerIndex,
            currentTrick = engine.currentTrick.toList(),
            winner = engine.winner
        )
    }

    fun getEngine(): Game400Engine = engine
}
