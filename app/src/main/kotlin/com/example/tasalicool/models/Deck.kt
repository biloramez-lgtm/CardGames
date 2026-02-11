package com.example.tasalicool.models

import java.io.Serializable
import kotlin.random.Random

data class Deck(
    val cards: MutableList<Card> = mutableListOf()
) : Serializable {

    init {
        if (cards.isEmpty()) {
            reset()
        }
    }

    /* ================= RESET ================= */

    fun reset() {
        cards.clear()

        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                cards.add(Card(suit, rank))
            }
        }

        shuffle()
    }

    /* ================= SHUFFLE ================= */

    fun shuffle() {
        cards.shuffle(Random(System.currentTimeMillis()))
    }

    /* ================= DRAW ================= */

    fun drawCard(): Card? {
        return if (cards.isNotEmpty()) {
            cards.removeAt(0)
        } else {
            null
        }
    }

    fun drawCards(count: Int): List<Card> {
        val safeCount = count.coerceAtMost(cards.size)
        val drawnCards = mutableListOf<Card>()

        repeat(safeCount) {
            drawCard()?.let { drawnCards.add(it) }
        }

        return drawnCards
    }

    /* ================= INFO ================= */

    fun remainingCards(): Int = cards.size

    fun isEmpty(): Boolean = cards.isEmpty()
}
