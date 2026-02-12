package com.example.tasalicool.models

import java.io.Serializable

/* ================= CONSTANTS ================= */

object Game400Constants {
    const val CARDS_PER_PLAYER = 13
    const val WIN_SCORE = 41
    val TRUMP_SUIT = Suit.HEARTS
}

/* ================= GAME LISTENER ================= */

interface GameEventListener {
    fun onCardsDealt(players: List<Player>)
    fun onCardPlayed(player: Player, card: Card)
    fun onTrickFinished(winner: Player, trickNumber: Int)
    fun onRoundFinished(players: List<Player>)
    fun onGameFinished(winner: Player)
}

/* ================= PURE GAME ENGINE ================= */

class Game400Engine(
    val players: List<Player>,
    private val listener: GameEventListener? = null
) : Serializable {

    val deck = Deck()

    var currentPlayerIndex = 0
    var trickNumber = 0
    val currentTrick = mutableListOf<Pair<Player, Card>>()

    var roundActive = false
    var gameWinner: Player? = null

    init {
        players.forEach { it.tricksWon = 0 }
    }

    /* ================= START ROUND ================= */

    fun startNewRound() {

        deck.reset()

        players.forEach {
            it.resetForNewRound()
            it.addCards(deck.drawCards(Game400Constants.CARDS_PER_PLAYER))
        }

        trickNumber = 0
        currentPlayerIndex = 0
        roundActive = true
        currentTrick.clear()

        listener?.onCardsDealt(players)
    }

    /* ================= GAME FLOW ================= */

    fun playCard(player: Player, card: Card): Boolean {

        if (!roundActive) return false
        if (player != getCurrentPlayer()) return false
        if (!isValidPlay(player, card)) return false

        player.removeCard(card)
        currentTrick.add(player to card)

        listener?.onCardPlayed(player, card)

        if (currentTrick.size == players.size)
            finishTrick()
        else
            nextPlayer()

        return true
    }

    fun getCurrentPlayer(): Player =
        players[currentPlayerIndex]

    private fun nextPlayer() {
        currentPlayerIndex =
            (currentPlayerIndex + 1) % players.size
    }

    /* ================= TRICK ================= */

    private fun finishTrick() {

        val winnerPair = determineTrickWinner() ?: return
        val winner = winnerPair.first

        winner.incrementTrick()

        currentPlayerIndex =
            players.indexOf(winner)

        currentTrick.clear()
        trickNumber++

        listener?.onTrickFinished(winner, trickNumber)

        if (trickNumber >= Game400Constants.CARDS_PER_PLAYER)
            finishRound()
    }

    private fun determineTrickWinner(): Pair<Player, Card>? {

        if (currentTrick.isEmpty()) return null

        val leadSuit = currentTrick.first().second.suit

        val trumpCards =
            currentTrick.filter {
                it.second.suit == Game400Constants.TRUMP_SUIT
            }

        return if (trumpCards.isNotEmpty())
            trumpCards.maxByOrNull { it.second.strength() }
        else
            currentTrick
                .filter { it.second.suit == leadSuit }
                .maxByOrNull { it.second.strength() }
    }

    /* ================= ROUND END ================= */

    private fun finishRound() {

        players.forEach { it.applyRoundScore() }

        listener?.onRoundFinished(players)

        checkGameWinner()
        roundActive = false
    }

    private fun checkGameWinner() {
        players.forEach {
            if (it.score >= Game400Constants.WIN_SCORE) {
                gameWinner = it
                listener?.onGameFinished(it)
            }
        }
    }

    /* ================= VALID PLAY ================= */

    private fun isValidPlay(
        player: Player,
        card: Card
    ): Boolean {

        if (currentTrick.isEmpty()) return true

        val leadSuit =
            currentTrick.first().second.suit

        val hasSuit =
            player.hand.any { it.suit == leadSuit }

        return if (hasSuit)
            card.suit == leadSuit
        else true
    }

    /* ================= GAME STATE ================= */

    fun isGameOver() =
        gameWinner != null
}
