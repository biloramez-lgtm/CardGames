package com.example.tasalicool.models

class PlayerLearningSystem {

    fun updateSkillLevel(currentLevel: Int, performanceScore: Int): Int {
        return when {
            performanceScore > 80 -> currentLevel + 1
            performanceScore < 30 -> (currentLevel - 1).coerceAtLeast(1)
            else -> currentLevel
        }
    }
}
