package com.example.tasalicool.models

import java.io.Serializable

data class GameState(

    val players: MutableList<Player> = mutableListOf(),

    var currentPlayerIndex: Int = 0,

    val deck: Deck = Deck(),

    val currentTrick: MutableList<Pair<Player, Card>> = mutableListOf(),

    var roundNumber: Int = 1,

    var gameInProgress: Boolean = true,

    var winner: Player? = null

) : Serializable {

    /* ================= CURRENT PLAYER ================= */

    fun getCurrentPlayer(): Player? {
        return if (players.isNotEmpty())
            players[currentPlayerIndex]
        else
            null
    }

    fun nextPlayer() {
        if (players.isEmpty()) return

        currentPlayerIndex =
            (currentPlayerIndex + 1) % players.size
    }

    /* ================= ROUND LOGIC ================= */

    fun isRoundFinished(): Boolean {
        if (players.isEmpty()) return true
        return players.all { it.hand.isEmpty() }
    }

    fun startNewRound() {
        roundNumber++
        currentTrick.clear()
        deck.reset()
        dealCards()
    }

    /* ================= CARD LOGIC ================= */

    fun playCard(player: Player, card: Card) {
        player.hand.remove(card)
        currentTrick.add(player to card)

        if (currentTrick.size == players.size) {
            evaluateTrick()
        } else {
            nextPlayer()
        }
    }

    private fun evaluateTrick() {
        // 🔥 هنا يمكنك وضع منطق تحديد الفائز في الجولة
        val winningPair = currentTrick.maxByOrNull { it.second.value }

        winningPair?.let { pair ->
            pair.first.score += 1
            currentPlayerIndex = players.indexOf(pair.first)
        }

        currentTrick.clear()
    }

    /* ================= DEAL CARDS ================= */

    fun dealCards(cardsPerPlayer: Int = 5) {
        players.forEach { player ->
            repeat(cardsPerPlayer) {
                deck.drawCard()?.let { card ->
                    player.hand.add(card)
                }
            }
        }
    }

    /* ================= TEAM SCORES ================= */

    fun getTeamScores(): Map<Int, Int> {
        return players
            .groupBy { it.teamId }
            .mapValues { entry ->
                entry.value.sumOf { it.score }
            }
    }

    /* ================= GAME END ================= */

    fun checkGameWinner(maxScore: Int = 10) {
        players.find { it.score >= maxScore }?.let {
            winner = it
            gameInProgress = false
        }
    }

    fun resetGame() {
        players.forEach {
            it.score = 0
            it.hand.clear()
        }

        currentPlayerIndex = 0
        roundNumber = 1
        winner = null
        gameInProgress = true
        currentTrick.clear()
        deck.reset()
    }
}
