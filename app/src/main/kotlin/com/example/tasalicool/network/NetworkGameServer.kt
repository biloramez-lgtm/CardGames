package com.example.tasalicool.network

import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class NetworkGameServer(private val port: Int = 5000) {

    private var serverSocket: ServerSocket? = null
    private val clients = CopyOnWriteArrayList<ClientConnection>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    private val isRunning = AtomicBoolean(false)
    private val playerCounter = AtomicInteger(1)

    /* ===================================================== */
    /* ================= START SERVER ====================== */
    /* ===================================================== */

    fun startServer(
        onClientConnected: (String) -> Unit = {},
        onClientDisconnected: (String) -> Unit = {},
        onMessageReceived: (NetworkMessage) -> Unit = {}
    ) {
        if (isRunning.get()) return

        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning.set(true)

                println("🔥 Server started on port $port")

                while (isActive && isRunning.get()) {

                    val socket = serverSocket?.accept() ?: continue

                    val playerId = "Player_${playerCounter.getAndIncrement()}"
                    val client = ClientConnection(socket, playerId)

                    clients.add(client)

                    println("✅ Client connected: $playerId")
                    onClientConnected(playerId)

                    // إعلام الجميع بانضمام لاعب
                    broadcastMessage(
                        NetworkMessage(
                            playerId = playerId,
                            gameType = "TASALI",
                            action = NetworkActions.PLAYER_JOINED,
                            data = null
                        )
                    )

                    listenToClient(
                        client,
                        onClientDisconnected,
                        onMessageReceived
                    )
                }

            } catch (e: Exception) {
                println("❌ Server error: ${e.message}")
            }
        }
    }

    /* ===================================================== */
    /* ================= LISTEN TO CLIENT ================== */
    /* ===================================================== */

    private fun listenToClient(
        client: ClientConnection,
        onClientDisconnected: (String) -> Unit,
        onMessageReceived: (NetworkMessage) -> Unit
    ) {
        scope.launch {
            try {
                while (isActive && isRunning.get()) {

                    val json = client.input.readUTF()
                    val message =
                        gson.fromJson(json, NetworkMessage::class.java)

                    onMessageReceived(message)

                    when (message.action) {

                        // توزيع أوراق
                        NetworkActions.DEAL_CARDS -> {
                            broadcastMessage(
                                message,
                                excludePlayer = null
                            )
                        }

                        // لعب ورقة
                        NetworkActions.PLAY_CARD -> {
                            broadcastMessage(
                                message,
                                excludePlayer = client.playerId
                            )
                        }

                        // تحديث حالة اللعبة
                        NetworkActions.GAME_STATE_UPDATE -> {
                            broadcastMessage(
                                message,
                                excludePlayer = client.playerId
                            )
                        }

                        // رسائل دردشة
                        NetworkActions.MESSAGE -> {
                            broadcastMessage(
                                message,
                                excludePlayer = null
                            )
                        }

                        // مغادرة
                        NetworkActions.PLAYER_LEFT -> {
                            removeClient(client, onClientDisconnected)
                        }

                        else -> {}
                    }
                }

            } catch (e: Exception) {
                removeClient(client, onClientDisconnected)
            }
        }
    }

    /* ===================================================== */
    /* ================= BROADCAST ========================= */
    /* ===================================================== */

    fun broadcastMessage(
        message: NetworkMessage,
        excludePlayer: String? = null
    ) {
        val json = gson.toJson(message)

        clients.forEach { client ->
            if (client.playerId == excludePlayer) return@forEach

            try {
                client.output.writeUTF(json)
                client.output.flush()
            } catch (e: Exception) {
                removeClient(client) {}
            }
        }
    }

    /* ===================================================== */
    /* ================= SEND TO ONE PLAYER ================= */
    /* ===================================================== */

    fun sendToPlayer(playerId: String, message: NetworkMessage) {
        val json = gson.toJson(message)

        clients.find { it.playerId == playerId }?.let { client ->
            try {
                client.output.writeUTF(json)
                client.output.flush()
            } catch (e: Exception) {
                removeClient(client) {}
            }
        }
    }

    /* ===================================================== */
    /* ================= REMOVE CLIENT ===================== */
    /* ===================================================== */

    private fun removeClient(
        client: ClientConnection,
        onClientDisconnected: (String) -> Unit
    ) {
        clients.remove(client)

        try { client.socket.close() } catch (_: Exception) {}

        println("🚪 Client disconnected: ${client.playerId}")

        broadcastMessage(
            NetworkMessage(
                playerId = client.playerId,
                gameType = "TASALI",
                action = NetworkActions.PLAYER_LEFT,
                data = null
            )
        )

        onClientDisconnected(client.playerId)
    }

    /* ===================================================== */
    /* ================= STOP SERVER ======================= */
    /* ===================================================== */

    fun stopServer() {
        isRunning.set(false)
        scope.cancel()

        clients.forEach {
            try { it.socket.close() } catch (_: Exception) {}
        }

        try { serverSocket?.close() } catch (_: Exception) {}

        println("🛑 Server stopped")
    }
}

/* ====================================================== */
/* ================= CLIENT CONNECTION ================== */
/* ====================================================== */

data class ClientConnection(
    val socket: Socket,
    val playerId: String
) {
    val input: DataInputStream = DataInputStream(socket.inputStream)
    val output: DataOutputStream = DataOutputStream(socket.outputStream)
}
