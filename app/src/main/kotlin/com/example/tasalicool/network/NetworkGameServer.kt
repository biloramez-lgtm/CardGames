package com.example.tasalicool.network

import com.example.tasalicool.models.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class NetworkGameServer(
    private val port: Int = 5000,
    private val gameEngine: Game400Engine
) {

    private var serverSocket: ServerSocket? = null
    private val clients = CopyOnWriteArrayList<ClientConnection>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    private val isRunning = AtomicBoolean(false)
    private val playerCounter = AtomicInteger(1)

    /* ================= START SERVER ================= */

    fun startServer(
        onClientConnected: (String) -> Unit = {},
        onClientDisconnected: (String) -> Unit = {},
        onGameUpdated: () -> Unit = {}
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

                    onClientConnected(playerId)

                    broadcastGameState()

                    listenToClient(client, onClientDisconnected, onGameUpdated)
                }

            } catch (e: Exception) {
                println("❌ Server error: ${e.message}")
            }
        }
    }

    /* ================= LISTEN ================= */

    private fun listenToClient(
        client: ClientConnection,
        onClientDisconnected: (String) -> Unit,
        onGameUpdated: () -> Unit
    ) {
        scope.launch {
            try {
                while (isActive && isRunning.get()) {

                    val json = client.input.readUTF()
                    val message =
                        gson.fromJson(json, NetworkMessage::class.java)

                    when (message.action) {

                        NetworkActions.PLAY_CARD -> {
                            handlePlayCard(message)
                            broadcastGameState()
                            onGameUpdated()
                        }

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

    /* ================= HANDLE PLAY ================= */

    private fun handlePlayCard(message: NetworkMessage) {

        val cardString = message.data ?: return
        val player = gameEngine.players
            .find { it.id == message.playerId } ?: return

        val card = Card.fromString(cardString) ?: return

        gameEngine.playCard(player, card)
    }

    /* ================= BROADCAST GAME STATE ================= */

    private fun broadcastGameState() {

        val stateJson = gson.toJson(gameEngine)

        val message = NetworkMessage(
            playerId = "SERVER",
            gameType = "GAME400",
            action = NetworkActions.GAME_STATE_UPDATE,
            data = stateJson
        )

        val json = gson.toJson(message)

        clients.forEach { client ->
            try {
                client.output.writeUTF(json)
                client.output.flush()
            } catch (_: Exception) {}
        }
    }

    /* ================= REMOVE ================= */

    private fun removeClient(
        client: ClientConnection,
        onClientDisconnected: (String) -> Unit
    ) {
        clients.remove(client)

        try { client.socket.close() } catch (_: Exception) {}

        onClientDisconnected(client.playerId)
    }

    /* ================= STOP ================= */

    fun stopServer() {
        isRunning.set(false)
        scope.cancel()

        clients.forEach {
            try { it.socket.close() } catch (_: Exception) {}
        }

        try { serverSocket?.close() } catch (_: Exception) {}
    }
}

/* ================= CLIENT CONNECTION ================= */

data class ClientConnection(
    val socket: Socket,
    val playerId: String
) {
    val input: DataInputStream = DataInputStream(socket.inputStream)
    val output: DataOutputStream = DataOutputStream(socket.outputStream)
}
