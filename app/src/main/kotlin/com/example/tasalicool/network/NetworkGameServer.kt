package com.example.tasalicool.network

import com.example.tasalicool.models.*
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

    private val isRunning = AtomicBoolean(false)
    private val playerCounter = AtomicInteger(1)

    /* ================= START ================= */

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
                    val playerId = "P${playerCounter.getAndIncrement()}"

                    val client = ClientConnection(socket, playerId)
                    clients.add(client)

                    onClientConnected(playerId)

                    listenToClient(client, onClientDisconnected, onGameUpdated)

                    sendFullStateTo(client)
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
                    val message = NetworkMessage.fromJson(json)

                    when (message.action) {

                        GameAction.JOIN -> {
                            sendFullStateTo(client)
                        }

                        GameAction.PLAY_CARD -> {
                            handlePlayCard(client, message)
                            broadcastFullState()
                            onGameUpdated()
                        }

                        GameAction.REQUEST_SYNC -> {
                            sendFullStateTo(client)
                        }

                        GameAction.LEAVE -> {
                            removeClient(client, onClientDisconnected)
                        }

                        GameAction.PING -> {
                            sendToClient(
                                client,
                                NetworkMessage(
                                    playerId = "SERVER",
                                    action = GameAction.PONG,
                                    isHost = true
                                )
                            )
                        }

                        else -> {}
                    }
                }

            } catch (_: Exception) {
                removeClient(client, onClientDisconnected)
            }
        }
    }

    /* ================= HANDLE PLAY ================= */

    private fun handlePlayCard(
        client: ClientConnection,
        message: NetworkMessage
    ) {

        if (!gameEngine.roundActive) return

        val cardString = message.payload ?: return

        val player = gameEngine.players
            .find { it.id == message.playerId }

        if (player == null) {
            sendError(client, "Invalid player")
            return
        }

        if (gameEngine.getCurrentPlayer().id != player.id) {
            sendError(client, "Not your turn")
            return
        }

        val card = Card.fromString(cardString)
        if (card == null) {
            sendError(client, "Invalid card")
            return
        }

        val success = gameEngine.playCard(player, card)

        if (!success) {
            sendError(client, "Illegal move")
        }
    }

    /* ================= STATE SYNC ================= */

    private fun broadcastFullState() {

        val stateJson =
            NetworkMessage.getGson().toJson(gameEngine)

        val message = NetworkMessage.createStateSync(
            hostId = "SERVER",
            stateJson = stateJson,
            round = gameEngine.currentRound,
            trick = gameEngine.trickNumber
        )

        clients.forEach {
            sendToClient(it, message)
        }
    }

    private fun sendFullStateTo(client: ClientConnection) {

        val stateJson =
            NetworkMessage.getGson().toJson(gameEngine)

        val message = NetworkMessage.createStateSync(
            hostId = "SERVER",
            stateJson = stateJson,
            round = gameEngine.currentRound,
            trick = gameEngine.trickNumber
        )

        sendToClient(client, message)
    }

    private fun sendToClient(
        client: ClientConnection,
        message: NetworkMessage
    ) {
        try {
            val json = NetworkMessage.toJson(message)
            client.output.writeUTF(json)
            client.output.flush()
        } catch (_: Exception) {}
    }

    private fun sendError(client: ClientConnection, error: String) {
        sendToClient(
            client,
            NetworkMessage.createError(client.playerId, error)
        )
    }

    /* ================= REMOVE ================= */

    private fun removeClient(
        client: ClientConnection,
        onClientDisconnected: (String) -> Unit
    ) {
        clients.remove(client)

        try { client.socket.close() } catch (_: Exception) {}

        onClientDisconnected(client.playerId)

        broadcastFullState()
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
    val input: DataInputStream =
        DataInputStream(socket.inputStream)

    val output: DataOutputStream =
        DataOutputStream(socket.outputStream)
}
