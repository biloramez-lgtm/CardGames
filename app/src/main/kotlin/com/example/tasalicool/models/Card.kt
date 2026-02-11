package com.example.tasalicool.models

import java.io.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/* =====================================================
   أنواع الأوراق
   ===================================================== */

enum class Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

/* =====================================================
   رتب الأوراق
   ===================================================== */

enum class Rank(val displayName: String, val value: Int) {
    ACE("A", 14),
    KING("K", 13),
    QUEEN("Q", 12),
    JACK("J", 11),
    TEN("10", 10),
    NINE("9", 9),
    EIGHT("8", 8),
    SEVEN("7", 7),
    SIX("6", 6),
    FIVE("5", 5),
    FOUR("4", 4),
    THREE("3", 3),
    TWO("2", 2)
}

/* =====================================================
   Card
   ===================================================== */

data class Card(
    val suit: Suit,
    val rank: Rank
) : Serializable {

    fun isTrump(): Boolean = suit == Suit.HEARTS

    fun strength(): Int =
        if (isTrump()) rank.value + 20 else rank.value
}

/* =====================================================
   Deck
   ===================================================== */

data class Deck(
    val cards: MutableList<Card> = mutableListOf()
) : Serializable {

    init {
        if (cards.isEmpty()) reset()
    }

    fun reset() {
        cards.clear()
        Suit.values().forEach { suit ->
            Rank.values().forEach { rank ->
                cards.add(Card(suit, rank))
            }
        }
        shuffle()
    }

    fun shuffle() = cards.shuffle()

    fun drawCard(): Card? =
        if (cards.isNotEmpty()) cards.removeAt(0) else null
}

/* =====================================================
   AI Difficulty
   ===================================================== */

enum class AIDifficulty {
    EASY, NORMAL, HARD, ELITE
}

/* =====================================================
   Player
   ===================================================== */

data class Player(
    val id: String,
    val name: String,
    val hand: MutableList<Card> = mutableListOf(),

    var score: Int = 0,
    var bid: Int = 0,
    var tricksWon: Int = 0,
    var teamId: Int = 0,
    val isLocal: Boolean = false,

    var difficulty: AIDifficulty = AIDifficulty.NORMAL,
    var rating: Int = 1200

) : Serializable {

    fun addCards(cards: List<Card>) {
        hand.addAll(cards)
        hand.sortByDescending { it.strength() }
    }

    fun removeCard(card: Card) = hand.remove(card)

    fun resetForNewRound() {
        bid = 0
        tricksWon = 0
        hand.clear()
    }

    fun incrementTrick() {
        tricksWon++
    }

    fun applyRoundScore(): Int {

        val points = when {
            bid == 13 ->
                if (tricksWon == 13) 400 else -52

            tricksWon >= bid ->
                if (bid >= 7) bid * 2 else bid

            else ->
                if (bid >= 7) -(bid * 2) else -bid
        }

        score += points
        return points
    }

    fun aggressionFactor(): Double =
        when (difficulty) {
            AIDifficulty.EASY -> 0.8
            AIDifficulty.NORMAL -> 1.0
            AIDifficulty.HARD -> 1.2
            AIDifficulty.ELITE -> 1.4
        }

    fun updateRating(opponentRating: Int, won: Boolean) {

        val kFactor = when (difficulty) {
            AIDifficulty.EASY -> 16
            AIDifficulty.NORMAL -> 24
            AIDifficulty.HARD -> 32
            AIDifficulty.ELITE -> 40
        }

        val expected =
            1.0 / (1 + Math.pow(10.0,
                (opponentRating - rating) / 400.0))

        val scoreValue = if (won) 1.0 else 0.0

        rating =
            (rating + kFactor *
                    (scoreValue - expected)).toInt()

        rating = max(800, min(3000, rating))
    }
}

/* =====================================================
   Round History
   ===================================================== */

data class RoundResult(
    val roundNumber: Int,
    val teamScores: Map<Int, Int>
) : Serializable

/* =====================================================
   GameState (Competitive Engine)
   ===================================================== */

data class GameState(

    val players: List<Player>,
    var currentPlayerIndex: Int = 0,
    val deck: Deck = Deck(),
    val currentTrick: MutableList<Pair<Player, Card>> = mutableListOf(),

    var roundNumber: Int = 1,
    var gameInProgress: Boolean = true,
    var winner: Player? = null,

    val matchHistory: MutableList<RoundResult> = mutableListOf()

) : Serializable {

    fun getCurrentPlayer(): Player =
        players[currentPlayerIndex]

    fun nextPlayer() {
        currentPlayerIndex =
            (currentPlayerIndex + 1) % players.size
    }

    /* ================= Card Play ================= */

    fun playCard(player: Player, card: Card): Boolean {

        if (!gameInProgress) return false
        if (player != getCurrentPlayer()) return false
        if (!player.hand.contains(card)) return false

        if (currentTrick.isNotEmpty()) {
            val leadSuit = currentTrick.first().second.suit
            val hasLead = player.hand.any { it.suit == leadSuit }
            if (hasLead && card.suit != leadSuit) return false
        }

        player.removeCard(card)
        currentTrick.add(player to card)

        if (currentTrick.size == 4) {
            resolveTrick()
        } else {
            nextPlayer()
        }

        return true
    }

    /* ================= Trick Logic ================= */

    private fun resolveTrick() {

        val leadSuit = currentTrick.first().second.suit

        val winnerPlay = currentTrick.maxByOrNull { (_, card) ->
            when {
                card.isTrump() -> card.strength() + 100
                card.suit == leadSuit -> card.strength()
                else -> 0
            }
        }!!

        val winningPlayer = winnerPlay.first
        winningPlayer.incrementTrick()

        currentTrick.clear()
        currentPlayerIndex = players.indexOf(winningPlayer)

        if (players.all { it.hand.isEmpty() }) {
            endRound()
        }
    }

    /* ================= Round End ================= */

    private fun endRound() {

        players.forEach { it.applyRoundScore() }

        recordRoundResult()

        checkGameEnd()

        players.forEach { it.resetForNewRound() }
        deck.reset()
    }

    private fun checkGameEnd() {

        val teamScores =
            players.groupBy { it.teamId }
                .mapValues { entry ->
                    entry.value.sumOf { it.score }
                }

        val winningTeam =
            teamScores.maxByOrNull { it.value }

        if (winningTeam != null &&
            winningTeam.value >= 400) {

            gameInProgress = false

            val winners =
                players.filter {
                    it.teamId == winningTeam.key
                }

            winner = winners.first()

            updateElo(winningTeam.key)
        }
    }

    /* ================= ELO ================= */

    private fun updateElo(winningTeamId: Int) {

        val team1 =
            players.filter { it.teamId == 1 }

        val team2 =
            players.filter { it.teamId == 2 }

        val avg1 =
            team1.map { it.rating }.average().toInt()

        val avg2 =
            team2.map { it.rating }.average().toInt()

        val team1Won =
            winningTeamId == 1

        team1.forEach {
            it.updateRating(avg2, team1Won)
        }

        team2.forEach {
            it.updateRating(avg1, !team1Won)
        }
    }

    fun recordRoundResult() {

        val teamScores =
            players.groupBy { it.teamId }
                .mapValues { entry ->
                    entry.value.sumOf { it.score }
                }

        matchHistory.add(
            RoundResult(roundNumber, teamScores)
        )

        roundNumber++
    }
}

/* =====================================================
   Game Type
   ===================================================== */

enum class GameType {
    GAME_400
}
