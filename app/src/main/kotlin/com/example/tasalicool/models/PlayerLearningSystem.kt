package com.example.tasalicool.models

class PlayerLearningSystem {

    private var skillLevel: Int = 1
    private val moveHistory = mutableListOf<Card>()

    /* ✅ مستخدمة داخل Game400Engine */
    fun recordPlayerMove(card: Card) {
        moveHistory.add(card)
    }

    /* ✅ مستخدمة داخل finishRound() */
    fun endRoundAnalysis() {

        val performanceScore = when {
            moveHistory.size > 10 -> 80
            moveHistory.isEmpty() -> 20
            else -> 50
        }

        skillLevel = updateSkillLevel(skillLevel, performanceScore)

        moveHistory.clear()
    }

    fun updateSkillLevel(currentLevel: Int, performanceScore: Int): Int {
        return when {
            performanceScore > 80 -> currentLevel + 1
            performanceScore < 30 -> (currentLevel - 1).coerceAtLeast(1)
            else -> currentLevel
        }
    }

    fun getSkillLevel(): Int {
        return skillLevel
    }
}
