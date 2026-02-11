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

    fun getCurrentPlayer(): Player? =
        if (players.isNotEmpty())
            players[currentPlayerIndex]
        else null

    fun nextPlayer() {
        if (players.isEmpty()) return
        currentPlayerIndex =
            (currentPlayerIndex + 1) % players.size
    }

    /* ================= ROUND CONTROL ================= */

    fun isRoundFinished(): Boolean {
        if (players.isEmpty()) return true
        return players.all { it.hand.isEmpty() }
    }

    fun startNewRound(cardsPerPlayer: Int = 5) {

        roundNumber++
        currentTrick.clear()
        deck.reset()

        players.forEach { it.resetForNewRound() }

        dealCards(cardsPerPlayer)

        currentPlayerIndex = 0
    }

    /* ================= PLAY CARD ================= */

    fun playCard(player: Player, card: Card) {

        if (player != getCurrentPlayer()) return
        if (!player.hand.contains(card)) return

        player.removeCard(card)

        currentTrick.add(player to card)

        if (currentTrick.size == players.size) {
            evaluateTrick()
        } else {
            nextPlayer()
        }
    }

    /* ================= TRICK EVALUATION ================= */

    private fun evaluateTrick() {

        // استخدام strength وليس value
        val winningPair =
            currentTrick.maxByOrNull { it.second.strength() }

        winningPair?.first?.incrementTrick()

        // اللاعب الفائز يبدأ الجولة التالية
        winningPair?.first?.let {
            currentPlayerIndex = players.indexOf(it)
        }

        currentTrick.clear()

        if (isRoundFinished()) {
            finishRound()
        }
    }

    /* ================= ROUND FINISH ================= */

    private fun finishRound() {

        players.forEach {
            it.applyRoundScore()
        }

        checkGameWinner()
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

    fun checkGameWinner(maxScore: Int = 400) {

        players.find { it.score >= maxScore }?.let {
            winner = it
            gameInProgress = false
        }
    }

    fun resetGame() {

        players.forEach {
            it.score = 0
            it.resetForNewRound()
        }

        currentPlayerIndex = 0
        roundNumber = 1
        winner = null
        gameInProgress = true
        currentTrick.clear()
        deck.reset()
    }
}
