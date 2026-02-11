package com.example.tasalicool.models

import java.io.Serializable

data class RoundResult(
    val roundNumber: Int,
    val teamScores: Map<Int, Int>
) : Serializable
