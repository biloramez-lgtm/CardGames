package com.example.tasalicool.models

import java.io.Serializable

data class Card(
    val suit: Suit,
    val rank: Rank
) : Serializable {

    /* ================= GAME LOGIC ================= */

    fun isTrump(): Boolean {
        return suit == Suit.HEARTS
    }

    fun strength(): Int {
        return if (isTrump()) {
            rank.value + 20
        } else {
            rank.value
        }
    }

    /* ================= NETWORK HELPERS ================= */

    // تحويل الكرت إلى Map لإرساله عبر الشبكة
    fun toNetworkMap(): Map<String, String> {
        return mapOf(
            "suit" to suit.name,
            "rank" to rank.name
        )
    }

    // تحويل الكرت إلى String
    override fun toString(): String {
        return "${suit.name}_${rank.name}"
    }

    companion object {

        // استرجاع الكرت من البيانات القادمة من الشبكة (Map)
        fun fromNetworkMap(map: Map<String, String>): Card? {
            return try {
                val suit = Suit.valueOf(map["suit"] ?: return null)
                val rank = Rank.valueOf(map["rank"] ?: return null)
                Card(suit, rank)
            } catch (e: Exception) {
                null
            }
        }

        // استرجاع الكرت من String
        fun fromString(cardString: String): Card? {
            return try {
                val parts = cardString.split("_")
                if (parts.size != 2) return null

                val suit = Suit.valueOf(parts[0])
                val rank = Rank.valueOf(parts[1])

                Card(suit, rank)
            } catch (e: Exception) {
                null
            }
        }
    }
}

enum class Suit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}

enum class Rank(val value: Int) {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13),
    ACE(14)
}
