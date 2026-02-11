package com.example.tasalicool.models

import com.google.gson.Gson
import java.io.Serializable

data class GameState(

    val players: MutableList<Player> = mutableListOf(),
    var currentPlayerIndex: Int = 0,
    val deck: Deck = Deck(),
    val currentTrick: MutableList<Pair<String, Card>> = mutableListOf(),
    var roundNumber: Int = 1,
    var gameInProgress: Boolean = false,
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
    /* ================= START GAME (HOST ONLY) ============ */
    /* ===================================================== */

    /**
     * يستخدم فقط من الـ HOST
     * يبدأ اللعبة ويعيد JSON جاهز للإرسال
     */
    fun startGameAndCreatePayload(cardsPerPlayer: Int = 5): String {

        if (players.isEmpty()) return ""

        roundNumber = 1
        currentPlayerIndex = 0
        winnerId = null
        gameInProgress = true

        deck.reset()
        deck.shuffle()

        players.forEach {
            it.score = 0
            it.resetForNewRound()
        }

        dealCards(cardsPerPlayer)

        return createNetworkPayload()
    }

    /* ===================================================== */
    /* ================= NEW ROUND (HOST) ================== */
    /* ===================================================== */

    fun startNewRoundAndCreatePayload(
        cardsPerPlayer: Int = 5
    ): String {

        roundNumber++
        currentTrick.clear()

        deck.reset()
        deck.shuffle()

        players.forEach { it.resetForNewRound() }

        dealCards(cardsPerPlayer)

        currentPlayerIndex = 0

        return createNetworkPayload()
    }

    /* ===================================================== */
    /* ================= PLAY CARD (HOST) ================== */
    /* ===================================================== */

    fun playCardAndCreatePayload(
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

        return createNetworkPayload()
    }

    /* ===================================================== */
    /* ================= APPLY FULL STATE (CLIENT) ========= */
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

        // تحديث اللاعبين
        networkState.players.forEach { netPlayer ->

            val localPlayer =
                players.find { it.id == netPlayer.id }

            if (localPlayer != null) {
                localPlayer.updateFromNetwork(netPlayer)
            } else {
                players.add(netPlayer)
            }
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
    /* ================= ROUND STATUS ====================== */
    /* ===================================================== */

    fun isRoundFinished(): Boolean {
        if (players.isEmpty()) return true
        return players.all { it.hand.isEmpty() }
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
        gameInProgress = false
        currentTrick.clear()
        deck.reset()
    }

    /* ===================================================== */
    /* ================= NETWORK PAYLOAD =================== */
    /* ===================================================== */

    /**
     * ينشئ نسخة آمنة للشبكة (بدون Deck)
     */
    private fun createNetworkPayload(): String {
        val safeState = toNetworkSafeCopy()
        return gson.toJson(safeState)
    }

    /**
     * Host فقط يحتفظ بالـ deck الحقيقي
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
