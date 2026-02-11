package com.example.tasalicool.models

import kotlin.math.max
import kotlin.math.min

object Game400AI {

    /* =========================================================
       🧠 تقييم قوة اليد (حساب احتمالي احترافي)
       ========================================================= */

    fun evaluateHandStrength(player: Player): Double {

        var score = 0.0

        val trumpCards = player.hand.filter { it.isTrump() }
        val highCards = player.hand.filter {
            it.rank.value >= 11
        }

        // وزن الطرنيب (مهم جداً)
        score += trumpCards.size * 2.5

        // وزن الأوراق العالية
        score += highCards.size * 1.5

        // آص الطرنيب أقوى شيء
        trumpCards.forEach {
            if (it.rank == Rank.ACE) score += 2
            if (it.rank == Rank.KING) score += 1
        }

        // تنوع الأنواع مفيد تكتيكياً
        val suitVariety = player.hand.map { it.suit }.distinct().size
        score += suitVariety * 0.3

        return score
    }

    /* =========================================================
       🎯 حساب الطلب الذكي (Bidding AI)
       ========================================================= */

    fun calculateBid(player: Player): Int {

        val strength = evaluateHandStrength(player)

        var bid = (strength / 3).toInt()

        // حدود منطقية
        bid = max(2, bid)
        bid = min(13, bid)

        // مخاطرة ذكية
        if (strength > 18) bid += 1
        if (strength > 22) bid += 1

        return min(bid, 13)
    }

    /* =========================================================
       🧠 اختيار أفضل ورقة للعب (AI خرافي)
       ========================================================= */

    fun chooseCard(
        player: Player,
        gameState: GameState
    ): Card {

        val currentTrick = gameState.currentTrick

        // إذا هو أول لاعب
        if (currentTrick.isEmpty()) {
            return chooseOpeningCard(player)
        }

        val leadSuit = currentTrick.first().second.suit

        val sameSuitCards = player.hand.filter { it.suit == leadSuit }

        // إذا عنده نفس النوع لازم يلعب منه
        if (sameSuitCards.isNotEmpty()) {

            val winningCard = findWinningCard(
                sameSuitCards,
                currentTrick
            )

            return winningCard ?: sameSuitCards.minBy { it.rank.value }
        }

        // ما عنده نفس النوع
        val trumpCards = player.hand.filter { it.isTrump() }

        if (trumpCards.isNotEmpty()) {

            val winningTrump = findWinningCard(
                trumpCards,
                currentTrick
            )

            return winningTrump ?: trumpCards.minBy { it.rank.value }
        }

        // ما عنده شي مفيد → يرمي أضعف ورقة
        return player.hand.minBy { it.rank.value }
    }

    /* ========================================================= */

    private fun chooseOpeningCard(player: Player): Card {

        val strongTrump = player.hand
            .filter { it.isTrump() }
            .maxByOrNull { it.rank.value }

        if (strongTrump != null && strongTrump.rank.value >= 12) {
            return strongTrump
        }

        return player.hand.maxBy { it.rank.value }
    }

    /* ========================================================= */

    private fun findWinningCard(
        candidateCards: List<Card>,
        currentTrick: List<Pair<Player, Card>>
    ): Card? {

        val highestOnTable = getHighestCard(currentTrick)

        return candidateCards
            .filter { compareCards(it, highestOnTable) > 0 }
            .minByOrNull { it.rank.value }
    }

    /* ========================================================= */

    private fun getHighestCard(
        trick: List<Pair<Player, Card>>
    ): Card {

        return trick.map { it.second }
            .reduce { acc, card ->
                if (compareCards(card, acc) > 0) card else acc
            }
    }

    /* ========================================================= */

    private fun compareCards(a: Card, b: Card): Int {

        // الطرنيب يغلب كل شيء
        if (a.isTrump() && !b.isTrump()) return 1
        if (!a.isTrump() && b.isTrump()) return -1

        // نفس النوع
        if (a.suit == b.suit) {
            return a.rank.value - b.rank.value
        }

        return 0
    }
}
