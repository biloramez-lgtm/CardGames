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

    companion object {

        // استرجاع الكرت من البيانات القادمة من الشبكة
        fun fromNetworkMap(map: Map<String, String>): Card? {
            return try {
                val suit = Suit.valueOf(map["suit"] ?: return null)
                val rank = Rank.valueOf(map["rank"] ?: return null)
                Card(suit, rank)
            } catch (e: Exception) {
                null
            }
        }
    }
}
