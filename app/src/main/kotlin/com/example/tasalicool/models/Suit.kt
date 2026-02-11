package com.example.tasalicool.models

import java.io.Serializable

enum class Suit(val displayName: String) : Serializable {

    HEARTS("♥ قلوب"),
    DIAMONDS("♦ ديناري"),
    CLUBS("♣ سباتي"),
    SPADES("♠ بستوني");

    companion object {
        fun fromName(name: String): Suit? {
            return values().find { it.name == name }
        }
    }
}
