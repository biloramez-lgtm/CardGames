package com.example.tasalicool.models

import com.google.gson.Gson
import java.io.Serializable

data class GameState(

    val players: MutableList<Player> = mutableListOf(),
    var currentPlayerIndex: Int = 0,
    val deck: Deck = Deck(),
    val currentTrick: MutableList<Pair<String, Card>> = mutableListOf(),
    var roundNumber: Int = 1,
    var gameInProgress: Boolean = true,
    var winnerId: String? = null

) : Serializable {

    private val gson = Gson()

    /* ===================================================== */
    /* ================= CURRENT PLAYER ==================== */
    /* ===================================================== */

    fun getCurrentPlayer(): Player? =
        players.getOrNull(currentPlayerIndex)

    fun nextPlayer() {
        if (players.isEmpty()) return
        currentPlayerIndex =
            (currentPlayerIndex + 1) % players.size
    }

    /* ===================================================== */
    /* ================= ROUND CONTROL ===================== */
    /* ===================================================== */

    fun isRoundFinished(): Boolean {
        if (players.isEmpty()) return true
        return players.all { it.hand.isEmpty() }
    }

    /**
     * يستخدم فقط من الـ HOST
     * يرجع JSON جاهز للإرسال
     */
    fun startNewRoundAndCreateNetworkPayload(
        cardsPerPlayer: Int = 5
    ): String {

        roundNumber++
        currentTrick.clear()
        deck.reset()
        deck.shuffle()

        players.forEach { it.resetForNewRound() }

        dealCards(cardsPerPlayer)

        currentPlayerIndex = 0

        val safeState = toNetworkSafeCopy()
        return gson.toJson(safeState)
    }

    /* ===================================================== */
    /* ================= PLAY CARD (HOST) ================== */
    /* ===================================================== */

    fun playCardAndCreateNetworkPayload(
        playerId: String,
        card: Card
    ): String? {

        val player = players.find { it.id == playerId }
            ?: return null

        if (!gameInProgress) return null
        if (player != getCurrentPlayer()) return null
        if (!player.hand.contains(card)) return null

        player.removeCard(card)
        currentTrick.add(player.id to card)

        if (currentTrick.size == players.size) {
            evaluateTrick()
        } else {
            nextPlayer()
        }

        val safeState = toNetworkSafeCopy()
        return gson.toJson(safeState)
    }

    /* ===================================================== */
    /* ================= APPLY NETWORK STATE =============== */
    /* ===================================================== */

    /**
     * يستخدم في الأجهزة CLIENT
     */
    fun applyFullNetworkState(json: String) {

        val networkState =
            gson.fromJson(json, GameState::class.java)

        currentPlayerIndex = networkState.currentPlayerIndex
        roundNumber = networkState.roundNumber
        gameInProgress = networkState.gameInProgress
        winnerId = networkState.winnerId

        currentTrick.clear()
        currentTrick.addAll(networkState.currentTrick)

        networkState.players.forEach { netPlayer ->
            players.find { it.id == netPlayer.id }
                ?.updateFromNetwork(netPlayer)
        }
    }

    /* ===================================================== */
    /* ================= TRICK EVALUATION ================== */
    /* ===================================================== */

    private fun evaluateTrick() {

        val winningPair =
            currentTrick.maxByOrNull { it.second.strength() }

        winningPair?.let { pair ->

            val winnerPlayer =
                players.find { it.id == pair.first }

            winnerPlayer?.incrementTrick()

            winnerPlayer?.let {
                currentPlayerIndex = players.indexOf(it)
            }
        }

        currentTrick.clear()

        if (isRoundFinished()) {
            finishRound()
        }
    }

    /* ===================================================== */
    /* ================= ROUND FINISH ====================== */
    /* ===================================================== */

    private fun finishRound() {

        players.forEach {
            it.applyRoundScore()
        }

        checkGameWinner()
    }

    /* ===================================================== */
    /* ================= DEAL CARDS ======================== */
    /* ===================================================== */

    private fun dealCards(cardsPerPlayer: Int = 5) {

        players.forEach { player ->
            val drawn = deck.drawCards(cardsPerPlayer)
            player.addCards(drawn)
        }
    }

    /* ===================================================== */
    /* ================= TEAM SCORES ======================= */
    /* ===================================================== */

    fun getTeamScores(): Map<Int, Int> {

        return players
            .groupBy { it.teamId }
            .mapValues { entry ->
                entry.value.sumOf { it.score }
            }
    }

    /* ===================================================== */
    /* ================= GAME END ========================== */
    /* ===================================================== */

    private fun checkGameWinner(maxScore: Int = 400) {

        players.find { it.score >= maxScore }?.let {
            winnerId = it.id
            gameInProgress = false
        }
    }

    fun resetGame() {

        players.forEach {
            it.score = 0
            it.resetForNewRound()
        }

        currentPlayerIndex = 0
        roundNumber = 1
        winnerId = null
        gameInProgress = true
        currentTrick.clear()
        deck.reset()
    }

    /* ===================================================== */
    /* ================= NETWORK SAFE COPY ================= */
    /* ===================================================== */

    /**
     * نسخة بدون Deck
     * Host فقط هو الذي يحتفظ بالـ deck الحقيقي
     */
    fun toNetworkSafeCopy(): GameState {

        val safePlayers =
            players.map { it.toNetworkSafeCopy() }
                .toMutableList()

        return copy(
            players = safePlayers,
            deck = Deck(mutableListOf())
        )
    }
}
