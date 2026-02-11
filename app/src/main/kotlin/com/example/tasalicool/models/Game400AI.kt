package com.example.tasalicool.models

import kotlin.math.max
import kotlin.math.min

object Game400AI {

    /* =========================================================
       🧠 تقييم قوة اليد (تحليل احترافي أعمق)
       ========================================================= */

    fun evaluateHandStrength(player: Player): Double {

        var score = 0.0

        val trumpCards = player.hand.filter { it.isTrump() }
        val highCards = player.hand.filter { it.rank.value >= 11 }

        // وزن عدد الطرنيب
        score += trumpCards.size * 3.0

        // وزن قوة الطرنيب نفسه
        trumpCards.forEach {
            score += when (it.rank) {
                Rank.ACE -> 3.5
                Rank.KING -> 2.5
                Rank.QUEEN -> 1.5
                else -> 0.5
            }
        }

        // وزن الأوراق العالية غير الطرنيب
        highCards.forEach {
            if (!it.isTrump()) score += 1.2
        }

        // توزيع الأنواع (قلة نوع = فرصة قطع)
        val suitCounts = player.hand.groupBy { it.suit }
        suitCounts.forEach { (_, cards) ->
            if (cards.size <= 2) score += 1.0
        }

        return score
    }

    /* =========================================================
       🎯 مزايدة أذكى
       ========================================================= */

    fun calculateBid(player: Player): Int {

        val strength = evaluateHandStrength(player)

        var bid = (strength / 4).toInt()

        bid = max(2, bid)
        bid = min(13, bid)

        if (strength > 20) bid += 1
        if (strength > 25) bid += 1

        return min(bid, 13)
    }

    /* =========================================================
       🧠 اختيار ورقة بذكاء تكتيكي
       ========================================================= */

    fun chooseCard(
        player: Player,
        gameState: GameState
    ): Card {

        val trick = gameState.currentTrick

        // إذا أول لاعب
        if (trick.isEmpty()) {
            return chooseOpeningCard(player)
        }

        val leadSuit = trick.first().second.suit
        val sameSuitCards = player.hand.filter { it.suit == leadSuit }
        val trumpCards = player.hand.filter { it.isTrump() }

        val currentWinner = getCurrentWinner(trick)
        val winningTeam = currentWinner?.first?.teamId
        val myTeam = player.teamId

        /* ==============================
           1️⃣ إذا عنده نفس النوع
           ============================== */

        if (sameSuitCards.isNotEmpty()) {

            // إذا فريقي رابح → لعب دفاع
            if (winningTeam == myTeam) {
                return sameSuitCards.minBy { it.rank.value }
            }

            // حاول تربح بأقل ورقة ممكنة
            val winningCard = currentWinner?.second

            val better = sameSuitCards
                .filter { compareCards(it, winningCard!!) > 0 }
                .minByOrNull { it.rank.value }

            return better ?: sameSuitCards.minBy { it.rank.value }
        }

        /* ==============================
           2️⃣ ما عنده نفس النوع → طرنيب؟
           ============================== */

        if (trumpCards.isNotEmpty()) {

            if (winningTeam == myTeam) {
                return player.hand.minBy { it.rank.value }
            }

            val winningCard = currentWinner?.second

            val betterTrump = trumpCards
                .filter { compareCards(it, winningCard!!) > 0 }
                .minByOrNull { it.rank.value }

            return betterTrump ?: player.hand.minBy { it.rank.value }
        }

        /* ==============================
           3️⃣ ما عنده شيء مفيد
           ============================== */

        return player.hand.minBy { it.rank.value }
    }

    /* ========================================================= */

    private fun chooseOpeningCard(player: Player): Card {

        val strongTrump = player.hand
            .filter { it.isTrump() }
            .maxByOrNull { it.rank.value }

        if (strongTrump != null && strongTrump.rank.value >= 13) {
            return strongTrump
        }

        // افتح بأقوى نوع تملكه بكثرة
        val grouped = player.hand.groupBy { it.suit }
        val strongestSuit = grouped.maxBy { it.value.size }.key

        return grouped[strongestSuit]!!
            .maxBy { it.rank.value }
    }

    /* ========================================================= */

    private fun getCurrentWinner(
        trick: List<Pair<Player, Card>>
    ): Pair<Player, Card>? {

        if (trick.isEmpty()) return null

        val leadSuit = trick.first().second.suit

        val trumpCards = trick.filter { it.second.isTrump() }

        return if (trumpCards.isNotEmpty()) {
            trumpCards.maxByOrNull { it.second.rank.value }
        } else {
            trick.filter { it.second.suit == leadSuit }
                .maxByOrNull { it.second.rank.value }
        }
    }

    /* ========================================================= */

    private fun compareCards(a: Card, b: Card): Int {

        if (a.isTrump() && !b.isTrump()) return 1
        if (!a.isTrump() && b.isTrump()) return -1

        if (a.suit == b.suit) {
            return a.rank.value - b.rank.value
        }

        return 0
    }
}
