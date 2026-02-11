package com.example.tasalicool.network

import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class NetworkGameClient {

    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val isConnected = AtomicBoolean(false)

    var playerId: String = "Player_${System.currentTimeMillis()}"

    /* ===================================================== */
    /* ======================== CONNECT ==================== */
    /* ===================================================== */

    fun connect(
        hostIp: String,
        port: Int = 5000,
        onConnected: () -> Unit = {},
        onMessageReceived: (NetworkMessage) -> Unit = {},
        onGameStateReceived: (String) -> Unit = {},
        onCardsReceived: (List<String>) -> Unit = {},
        onDisconnected: () -> Unit = {}
    ) {
        if (isConnected.get()) return

        scope.launch {
            try {
                socket = Socket(hostIp, port)
                input = DataInputStream(socket!!.inputStream)
                output = DataOutputStream(socket!!.outputStream)

                isConnected.set(true)

                println("Connected to server: $hostIp:$port")
                onConnected()

                // إرسال رسالة انضمام
                sendMessage(
                    NetworkMessage(
                        playerId = playerId,
                        gameType = "LOCAL_WIFI",
                        action = NetworkActions.PLAYER_JOINED
                    )
                )

                listen(
                    onMessageReceived,
                    onGameStateReceived,
                    onCardsReceived,
                    onDisconnected
                )

            } catch (e: Exception) {
                isConnected.set(false)
                println("Connection failed: ${e.message}")
                onDisconnected()
            }
        }
    }

    /* ===================================================== */
    /* ======================== LISTEN ===================== */
    /* ===================================================== */

    private fun listen(
        onMessageReceived: (NetworkMessage) -> Unit,
        onGameStateReceived: (String) -> Unit,
        onCardsReceived: (List<String>) -> Unit,
        onDisconnected: () -> Unit
    ) {
        scope.launch {
            try {
                while (isActive && isConnected.get()) {

                    val json = input?.readUTF() ?: break
                    val message = gson.fromJson(json, NetworkMessage::class.java)

                    when (message.action) {

                        NetworkActions.GAME_STATE_UPDATE -> {
                            val state = message.payload?.get("state")
                            if (state != null) {
                                onGameStateReceived(state)
                            }
                        }

                        NetworkActions.DEAL_CARDS -> {
                            val cardsJson = message.payload?.get("cards")
                            if (cardsJson != null) {
                                val cards = gson.fromJson(
                                    cardsJson,
                                    Array<String>::class.java
                                ).toList()
                                onCardsReceived(cards)
                            }
                        }

                        else -> {
                            onMessageReceived(message)
                        }
                    }
                }
            } catch (e: Exception) {
                println("Disconnected from server: ${e.message}")
            } finally {
                disconnectInternal()
                onDisconnected()
            }
        }
    }

    /* ===================================================== */
    /* ======================== SEND ======================= */
    /* ===================================================== */

    fun sendMessage(message: NetworkMessage) {
        if (!isConnected.get()) return

        scope.launch {
            try {
                val json = gson.toJson(message)
                output?.writeUTF(json)
                output?.flush()
            } catch (e: Exception) {
                disconnectInternal()
            }
        }
    }

    /* ================= SEND GAME STATE ================= */

    fun sendGameState(gameStateJson: String) {
        sendMessage(
            NetworkMessage(
                playerId = playerId,
                gameType = "GAME",
                action = NetworkActions.GAME_STATE_UPDATE,
                payload = mapOf("state" to gameStateJson)
            )
        )
    }

    /* ================= SEND CARDS ================= */

    fun sendCards(cards: List<String>) {
        sendMessage(
            NetworkMessage(
                playerId = playerId,
                gameType = "GAME",
                action = NetworkActions.REQUEST_CARDS,
                payload = mapOf(
                    "cards" to gson.toJson(cards)
                )
            )
        )
    }

    /* ================= PLAY CARD ================= */

    fun playCard(card: String) {
        sendMessage(
            NetworkMessage(
                playerId = playerId,
                gameType = "GAME",
                action = NetworkActions.PLAY_CARD,
                payload = mapOf("card" to card)
            )
        )
    }

    /* ===================================================== */
    /* ===================== DISCONNECT ==================== */
    /* ===================================================== */

    fun disconnect() {
        if (!isConnected.get()) return

        try {
            sendMessage(
                NetworkMessage(
                    playerId = playerId,
                    gameType = "LOCAL_WIFI",
                    action = NetworkActions.PLAYER_LEFT
                )
            )
        } catch (_: Exception) {}

        disconnectInternal()
    }

    private fun disconnectInternal() {
        isConnected.set(false)

        try { socket?.close() } catch (_: Exception) {}
        socket = null
        input = null
        output = null

        println("Client connection closed")
    }
}
