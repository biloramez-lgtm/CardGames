package com.example.tasalicool.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tasalicool.models.Game400Engine

class GameViewModel : ViewModel() {

    val engineState = mutableStateOf(Game400Engine())

    init {
        engineState.value.onGameUpdated = {
            engineState.value = engineState.value
        }
        engineState.value.startGame()
    }
}
