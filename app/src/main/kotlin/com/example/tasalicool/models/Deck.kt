package com.example.tasalicool.models

import java.io.Serializable

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

    fun drawCards(count: Int): List<Card> {
        val drawn = mutableListOf<Card>()
        repeat(count) {
            drawCard()?.let { drawn.add(it) }
        }
        return drawn
    }
}
