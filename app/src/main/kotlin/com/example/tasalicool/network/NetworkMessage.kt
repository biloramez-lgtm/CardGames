package com.example.tasalicool.network

data class NetworkMessage(
    val playerId: String,
    val gameType: String,
    val action: String,
    val payload: Map<String, String>? = null
)
