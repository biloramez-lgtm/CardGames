package com.example.tasalicool.game

import com.example.tasalicool.models.*
import kotlin.math.max

object AdvancedAI {

    /* ================= MEMORY ================= */

    private val playedCards = mutableSetOf<Card>()
    private val suitCount = mutableMapOf<Suit, Int>()
    private val playerMissingSuit =
        mutableMapOf<String, MutableSet<Suit>>()

    fun rememberCard(
        player: Player,
        card: Card,
        engine: Game400Engine
    ) {
        playedCards.add(card)

        suitCount[card.suit] =
            suitCount.getOrDefault(card.suit, 0) + 1

        val leadSuit =
            engine.currentTrick.firstOrNull()?.second?.suit

        if (leadSuit != null && card.suit != leadSuit) {
            val missing =
                playerMissingSuit.getOrPut(player.id) {
                    mutableSetOf()
                }
            missing.add(leadSuit)
        }
    }

    fun resetMemory() {
        playedCards.clear()
        suitCount.clear()
        playerMissingSuit.clear()
    }

    /* ================= BID LOGIC ================= */

    fun chooseBid(
        player: Player,
        engine: Game400Engine,
        minBid: Int
    ): Int {

        val strength =
            evaluateHandStrength(player, engine.trumpSuit)

        var bid = (strength / 5).toInt()

        if (strength > 30) bid++
        if (strength > 36) bid++

        // حذر بعد 30 نقطة
        if (player.score >= 30) bid--

        val maxPossible = 13 - engine.trickNumber
        bid = bid.coerceAtMost(maxPossible)

        return bid.coerceIn(minBid, 13)
    }

    private fun evaluateHandStrength(
        player: Player,
        trump: Suit
    ): Double {

        var score = 0.0

        val trumpCards =
            player.hand.filter { it.isTrump(trump) }

        score += trumpCards.size * 4

        trumpCards.forEach {
            score += when (it.rank) {
                Rank.ACE -> 5.0
                Rank.KING -> 4.0
                Rank.QUEEN -> 3.0
                Rank.JACK -> 2.0
                else -> 1.0
            }
        }

        player.hand
            .filter { it.rank.value >= 11 }
            .forEach { score += 1.2 }

        return score
    }

    /* ================= CARD DECISION ================= */

    fun chooseCard(
        player: Player,
        engine: Game400Engine
    ): Card {

        val trick = engine.currentTrick
        val validCards = getValidCards(player, trick)
        val trump = engine.trumpSuit

        var bestCard = validCards.first()
        var bestScore = Double.NEGATIVE_INFINITY

        for (card in validCards) {

            val winChance =
                calculateWinProbability(player, card, engine)

            val tactical =
                tacticalFactor(player, card, engine)

            val partner =
                partnerFactor(player, trick, trump)

            val score =
                winChance * 0.5 +
                tactical * 0.3 +
                partner * 0.2

            if (score > bestScore) {
                bestScore = score
                bestCard = card
            }
        }

        return bestCard
    }

    /* ================= WIN PROBABILITY ================= */

    private fun calculateWinProbability(
        player: Player,
        card: Card,
        engine: Game400Engine
    ): Double {

        val trump = engine.trumpSuit
        val remaining = buildRemainingDeck(player, card)

        val higher =
            remaining.count {
                it.suit == card.suit &&
                        it.rank.value > card.rank.value
            }

        val trumpThreat =
            remaining.count {
                it.isTrump(trump) && !card.isTrump(trump)
            }

        val total = remaining.size.toDouble()
        if (total == 0.0) return 1.0

        var risk = (higher + trumpThreat) / total

        // ضغط حسب عدد اللاعبين المتبقين
        val remainingPlayers =
            engine.players.size -
                    engine.currentTrick.size - 1

        repeat(max(0, remainingPlayers)) {
            risk *= 1.1
        }

        return (1.0 - risk).coerceIn(0.0, 1.0)
    }

    private fun buildRemainingDeck(
        player: Player,
        card: Card
    ): List<Card> {

        val all =
            Suit.values().flatMap { suit ->
                Rank.values().map { rank ->
                    Card(suit, rank)
                }
            }

        return all
            .filterNot { playedCards.contains(it) }
            .filterNot { player.hand.contains(it) }
            .filterNot { it == card }
    }

    /* ================= TACTICAL ================= */

    private fun tacticalFactor(
        player: Player,
        card: Card,
        engine: Game400Engine
    ): Double {

        val trump = engine.trumpSuit
        var score = card.rank.value / 14.0

        if (card.isTrump(trump))
            score += 0.6

        val needed = player.bid - player.tricksWon
        val remaining = 13 - engine.trickNumber

        when {
            needed <= 0 -> score -= 1.0
            needed >= remaining -> score += 1.2
            needed > remaining / 2 -> score += 0.6
        }

        return score
    }

    /* ================= PARTNER LOGIC ================= */

    private fun partnerFactor(
        player: Player,
        trick: List<Pair<Player, Card>>,
        trump: Suit
    ): Double {

        if (trick.isEmpty()) return 0.0

        val currentWinner =
            determineCurrentWinner(trick, trump)

        return if (currentWinner?.teamId ==
            player.teamId
        )
            -0.6
        else 0.4
    }

    private fun determineCurrentWinner(
        trick: List<Pair<Player, Card>>,
        trump: Suit
    ): Player? {

        if (trick.isEmpty()) return null

        val leadSuit = trick.first().second.suit

        val trumpCards =
            trick.filter { it.second.isTrump(trump) }

        return if (trumpCards.isNotEmpty())
            trumpCards.maxBy { it.second.rank.value }.first
        else
            trick.filter { it.second.suit == leadSuit }
                .maxBy { it.second.rank.value }
                .first
    }

    /* ================= VALID CARDS ================= */

    private fun getValidCards(
        player: Player,
        trick: List<Pair<Player, Card>>
    ): List<Card> {

        if (trick.isEmpty())
            return player.hand

        val leadSuit = trick.first().second.suit
        val hasSuit =
            player.hand.any { it.suit == leadSuit }

        return if (hasSuit)
            player.hand.filter { it.suit == leadSuit }
        else player.hand
    }
}
