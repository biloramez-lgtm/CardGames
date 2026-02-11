package com.example.tasalicool.models

data class GameState(
    val playerHands: List<List<Card>>,
    val tableCards: List<Card>,
    val currentPlayer: Int,
    val scores: List<Int>,
    val roundNumber: Int
)
