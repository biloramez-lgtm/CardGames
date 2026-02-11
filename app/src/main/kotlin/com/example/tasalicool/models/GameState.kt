package com.example.tasalicool.models

import java.io.Serializable

data class GameState(

    val players: List<Player>,
    var currentPlayerIndex: Int = 0,
    val deck: Deck = Deck(),
    val currentTrick: MutableList<Pair<Player, Card>> = mutableListOf(),

    var roundNumber: Int = 1,
    var gameInProgress: Boolean = true,
    var winner: Player? = null

) : Serializable {

    fun getCurrentPlayer(): Player =
        players[currentPlayerIndex]

    fun nextPlayer() {
        currentPlayerIndex =
            (currentPlayerIndex + 1) % players.size
    }

    fun isRoundFinished(): Boolean {
        return players.all { it.hand.isEmpty() }
    }

    fun getTeamScores(): Map<Int, Int> {
        return players
            .groupBy { it.teamId }
            .mapValues { entry ->
                entry.value.sumOf { it.score }
            }
    }
}
