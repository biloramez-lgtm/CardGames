package com.example.tasalicool.ui.state

import com.example.tasalicool.models.Card
import com.example.tasalicool.models.GamePhase
import com.example.tasalicool.models.Player

data class GameUiState(

    /* ================= GAME STATE ================= */

    val phase: GamePhase = GamePhase.BIDDING,

    val players: List<Player> = emptyList(),

    val currentPlayerIndex: Int = 0,

    val currentTrick: List<Pair<Player, Card>> = emptyList(),

    val winner: Player? = null,

    /* ================= UI STATE ================= */

    val team1Score: Int = 0,

    val team2Score: Int = 0,

    val showBidDialog: Boolean = false
) {

    // 🔥 لاعب الدور الحالي بشكل آمن
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)

    // 🔥 تحقق من جاهزية اللاعبين
    val isReady: Boolean
        get() = players.size >= 4
}
