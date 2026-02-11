package com.example.tasalicool.models

import android.content.Context
import android.content.SharedPreferences
import java.io.Serializable
import kotlin.math.*

object Game400Constants {
    const val CARDS_PER_PLAYER = 13
    const val WIN_SCORE = 41
    val TRUMP_SUIT = Suit.HEARTS
}

class Game400Engine(
    context: Context,
    val players: List<Player>
) : Serializable {

    // ✅ نحفظ Application Context فقط (آمن)
    private val appContext = context.applicationContext

    val deck = Deck()

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(
            "FINAL_ELITE_AI",
            Context.MODE_PRIVATE
        )

    private val elo = EloRating(prefs)
    private val learning = PlayerLearningSystem(prefs)

    var currentPlayerIndex = 0
    var trickNumber = 0
    val currentTrick = mutableListOf<Pair<Player, Card>>()

    var roundActive = false
    var gameWinner: Player? = null

    private val playedCards = mutableSetOf<Card>()
    private val voidMemory =
        mutableMapOf<Player, MutableSet<Suit>>()

    init {
        players.forEach { voidMemory[it] = mutableSetOf() }
    }

    /* ================= START ROUND ================= */

    fun startNewRound() {

        deck.reset()
        playedCards.clear()

        players.forEach {
            it.resetForNewRound()
            it.addCards(deck.drawCards(Game400Constants.CARDS_PER_PLAYER))
            voidMemory[it]?.clear()
        }

        runSmartBidding()

        trickNumber = 0
        currentPlayerIndex = 0
        roundActive = true
        currentTrick.clear()
    }

    /* ================= SMART BIDDING ================= */

    private fun runSmartBidding() {
        players.forEach {
            if (!it.isLocal)
                it.bid = calculateFinalBid(it)
        }
    }

    private fun calculateFinalBid(player: Player): Int {

        var strength = 0.0
        val grouped = player.hand.groupBy { it.suit }

        grouped.forEach { (suit, cards) ->

            val high = cards.count { it.rank.value >= 11 }
            strength += high * 1.3

            if (cards.size >= 5)
                strength += 1.6

            if (suit == Game400Constants.TRUMP_SUIT)
                strength += cards.size * 0.6
        }

        strength += learning.getPlayerAggression() * 0.6
        strength += elo.rating / 1700.0

        return (strength / 2.0)
            .roundToInt()
            .coerceIn(1, 9)
    }

    /* ================= AI TURN ================= */

    fun playAITurnIfNeeded() {

        val current = getCurrentPlayer()

        if (!current.isLocal && roundActive) {
            val card = chooseFinalMove(current)
            playCard(current, card)
            playAITurnIfNeeded()
        }
    }

    private fun chooseFinalMove(player: Player): Card {

        val valid =
            player.hand.filter { isValidPlay(player,it) }

        var best = valid.first()
        var bestScore = Double.NEGATIVE_INFINITY

        for (card in valid) {

            val probability = calculateWinProbability(player, card)
            val tactical = tacticalEvaluation(player, card)
            val stage = stageFactor()
            val partner = partnerFactor(player)
            val risk = riskFactor(player)

            val score =
                probability * 0.40 +
                tactical * 0.25 +
                stage * 0.15 +
                partner * 0.10 -
                risk * 0.10

            if (score > bestScore) {
                bestScore = score
                best = card
            }
        }

        return best
    }

    /* ================= PROBABILITY ================= */

    private fun calculateWinProbability(
        player: Player,
        card: Card
    ): Double {

        val remaining = buildRemainingDeck(player, card)

        val higherSameSuit =
            remaining.count {
                it.suit == card.suit &&
                        it.rank.value > card.rank.value
            }

        val trumpThreat =
            remaining.count {
                it.suit == Game400Constants.TRUMP_SUIT &&
                        card.suit != Game400Constants.TRUMP_SUIT
            }

        val total = remaining.size.toDouble()
        if (total == 0.0) return 1.0

        val risk =
            (higherSameSuit + trumpThreat * 0.8) / total

        return 1.0 - risk
    }

    private fun buildRemainingDeck(
        player: Player,
        card: Card
    ): List<Card> {

        val all =
            Suit.values().flatMap { s ->
                Rank.values().map { r ->
                    Card(s, r)
                }
            }

        return all
            .filterNot { playedCards.contains(it) }
            .filterNot { player.hand.contains(it) }
            .filterNot { it == card }
    }

    /* ================= TACTICAL ================= */

    private fun tacticalEvaluation(
        player: Player,
        card: Card
    ): Double {

        var score = card.rank.value / 14.0

        if (card.suit == Game400Constants.TRUMP_SUIT)
            score += 0.9

        val needed =
            player.bid - player.tricksWon

        if (needed > 0)
            score += 0.6
        else
            score -= 0.3

        return score
    }

    private fun stageFactor(): Double {
        return when {
            trickNumber < 4 -> 0.3
            trickNumber < 9 -> 0.6
            else -> 1.0
        }
    }

    private fun partnerFactor(player: Player): Double {

        val partner =
            players.firstOrNull {
                it.teamId == player.teamId && it != player
            } ?: return 0.0

        val currentWinner =
            currentTrick.maxByOrNull { it.second.rank.value }?.first

        return if (currentWinner == partner)
            -0.5
        else 0.3
    }

    private fun riskFactor(player: Player): Double {

        val needed = player.bid - player.tricksWon
        val remaining = 13 - trickNumber

        return when {
            needed > remaining -> 1.0
            needed <= 0 -> 0.2
            else -> 0.5
        }
    }

    /* ================= GAME FLOW ================= */

    fun playCard(player: Player, card: Card): Boolean {

        if (!roundActive) return false
        if (player != getCurrentPlayer()) return false
        if (!isValidPlay(player, card)) return false

        if (player.isLocal) {
            learning.recordPlayerMove(card)
        }

        player.removeCard(card)
        currentTrick.add(player to card)
        playedCards.add(card)

        if (currentTrick.size == 4)
            finishTrick()
        else
            nextPlayer()

        return true
    }

    fun getCurrentPlayer() =
        players[currentPlayerIndex]

    private fun nextPlayer() {
        currentPlayerIndex =
            (currentPlayerIndex + 1) % players.size
    }

    private fun finishTrick() {

        val winner = determineTrickWinner()
        winner.first.incrementTrick()

        currentPlayerIndex =
            players.indexOf(winner.first)

        currentTrick.clear()
        trickNumber++

        if (trickNumber == 13)
            finishRound()
    }

    private fun determineTrickWinner():
            Pair<Player, Card> {

        val lead =
            currentTrick.first().second.suit

        val trump =
            currentTrick.filter {
                it.second.suit ==
                        Game400Constants.TRUMP_SUIT
            }

        return if (trump.isNotEmpty())
            trump.maxBy { it.second.rank.value }
        else
            currentTrick
                .filter { it.second.suit == lead }
                .maxBy { it.second.rank.value }
    }

    private fun finishRound() {

        learning.endRoundAnalysis()

        val winningTeam =
            players.groupBy { it.teamId }
                .maxBy {
                    it.value.sumOf { p -> p.tricksWon }
                }.key

        val aiWon =
            winningTeam != players.first().teamId

        elo.update(aiWon)

        players.forEach { it.applyRoundScore() }
        checkGameWinner()

        roundActive = false
    }

    private fun checkGameWinner() {
        players.forEach {
            if (it.score >= Game400Constants.WIN_SCORE)
                gameWinner = it
        }
    }

    private fun isValidPlay(
        player: Player,
        card: Card
    ): Boolean {

        if (currentTrick.isEmpty()) return true

        val lead =
            currentTrick.first().second.suit

        val hasSuit =
            player.hand.any { it.suit == lead }

        return if (hasSuit)
            card.suit == lead
        else true
    }

    fun isGameOver() =
        gameWinner != null
}
