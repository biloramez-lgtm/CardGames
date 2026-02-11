package com.example.tasalicool.models

import kotlin.math.max
import kotlin.math.min

object Game400AI {

    /* =========================================================
       🧠 ذاكرة عالمية للأوراق الملعوبة
       ========================================================= */

    private val playedCards = mutableSetOf<Card>()

    fun rememberCard(card: Card) {
        playedCards.add(card)
    }

    fun resetMemory() {
        playedCards.clear()
    }

    /* =========================================================
       🧠 تقييم قوة اليد (Hybrid محسّن)
       ========================================================= */

    fun evaluateHandStrength(player: Player): Double {

        var score = 0.0

        val trumpCards = player.hand.filter { it.isTrump() }
        val highCards = player.hand.filter { it.rank.value >= 11 }

        score += trumpCards.size * 3.5

        trumpCards.forEach {
            score += when (it.rank) {
                Rank.ACE -> 4.0
                Rank.KING -> 3.0
                Rank.QUEEN -> 2.0
                else -> 1.0
            }
        }

        highCards.forEach {
            if (!it.isTrump()) score += 1.5
        }

        val suitCounts = player.hand.groupBy { it.suit }
        suitCounts.forEach { (_, cards) ->
            if (cards.size <= 2) score += 1.5
        }

        return score
    }

    /* =========================================================
       🎯 مزايدة هجومية ذكية
       ========================================================= */

    fun calculateBid(player: Player): Int {

        val strength = evaluateHandStrength(player)

        var bid = (strength / 4).toInt()

        bid = max(2, bid)
        bid = min(13, bid)

        if (strength > 22) bid += 1
        if (strength > 28) bid += 1

        return min(bid, 13)
    }

    /* =========================================================
       🧠 اختيار ورقة – Hybrid Elite
       ========================================================= */

    fun chooseCard(
        player: Player,
        gameState: GameState
    ): Card {

        val trick = gameState.currentTrick
        val validCards = getValidCards(player, trick)

        var bestCard = validCards.first()
        var bestScore = Double.NEGATIVE_INFINITY

        for (card in validCards) {

            val probability = calculateWinProbability(player, card)
            val tactical = tacticalEvaluation(player, card)
            val stage = stageFactor(gameState)
            val partner = partnerFactor(player, trick)
            val risk = riskFactor(player, gameState)

            val score =
                probability * 0.40 +
                tactical * 0.25 +
                stage * 0.15 +
                partner * 0.10 -
                risk * 0.10

            if (score > bestScore) {
                bestScore = score
                bestCard = card
            }
        }

        return bestCard
    }

    /* =========================================================
       🎯 حساب الاحتمال الحقيقي للفوز
       ========================================================= */

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
                it.isTrump() &&
                        !card.isTrump()
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

    /* ========================================================= */

    private fun tacticalEvaluation(
        player: Player,
        card: Card
    ): Double {

        var score = card.rank.value / 14.0

        if (card.isTrump())
            score += 0.9

        val needed =
            player.bid - player.tricksWon

        if (needed > 0)
            score += 0.6
        else
            score -= 0.3

        return score
    }

    private fun stageFactor(
        gameState: GameState
    ): Double {

        val trickNumber =
            gameState.players.sumOf { it.tricksWon }

        return when {
            trickNumber < 4 -> 0.3
            trickNumber < 9 -> 0.6
            else -> 1.0
        }
    }

    private fun partnerFactor(
        player: Player,
        trick: List<Pair<Player, Card>>
    ): Double {

        val partner =
            trick.firstOrNull {
                it.first.teamId == player.teamId &&
                        it.first != player
            }?.first

        val currentWinner =
            trick.maxByOrNull { it.second.rank.value }?.first

        return if (currentWinner == partner)
            -0.5
        else 0.3
    }

    private fun riskFactor(
        player: Player,
        gameState: GameState
    ): Double {

        val totalTricks =
            gameState.players.sumOf { it.tricksWon }

        val remaining = 13 - totalTricks
        val needed = player.bid - player.tricksWon

        return when {
            needed > remaining -> 1.0
            needed <= 0 -> 0.2
            else -> 0.5
        }
    }

    /* ========================================================= */

    private fun getValidCards(
        player: Player,
        trick: List<Pair<Player, Card>>
    ): List<Card> {

        if (trick.isEmpty())
            return player.hand

        val leadSuit =
            trick.first().second.suit

        val hasSuit =
            player.hand.any { it.suit == leadSuit }

        return if (hasSuit)
            player.hand.filter { it.suit == leadSuit }
        else player.hand
    }
}
