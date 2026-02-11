package com.example.tasalicool.models

import java.io.Serializable

data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val deck: Deck,
    val currentTrick: List<Pair<Player, Card>>,
    val roundNumber: Int,
    val gameInProgress: Boolean,
    val winner: Player?
) : Serializable {

    fun getCurrentPlayer(): Player {
        return players[currentPlayerIndex]
    }

    fun getLeadSuit(): Suit? {
        return currentTrick.firstOrNull()?.second?.suit
    }

    fun isTrickEmpty(): Boolean {
        return currentTrick.isEmpty()
    }

    fun getPlayedCards(): List<Card> {
        return currentTrick.map { it.second }
    }
}
