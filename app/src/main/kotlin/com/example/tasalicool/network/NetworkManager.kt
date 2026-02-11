package com.example.tasalicool.network

import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class NetworkGameServer(private val port: Int = 5000) {

    private var serverSocket: ServerSocket? = null
    private val clients = CopyOnWriteArrayList<ClientConnection>()
    private val gson = Gson()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /* ================= START SERVER ================= */

    fun startServer(
        onClientConnected: (String) -> Unit = {},
        onMessageReceived: (NetworkMessage) -> Unit = {}
    ) {
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                println("Server started on port $port")

                while (isActive) {
                    val socket = serverSocket?.accept() ?: continue
                    val client = ClientConnection(socket)
                    clients.add(client)

                    onClientConnected(client.playerId)

                    listenToClient(client, onMessageReceived)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /* ================= LISTEN TO CLIENT ================= */

    private fun listenToClient(
        client: ClientConnection,
        onMessageReceived: (NetworkMessage) -> Unit
    ) {
        scope.launch {
            try {
                while (isActive) {
                    val json = client.input.readUTF()
                    val message =
                        gson.fromJson(json, NetworkMessage::class.java)

                    onMessageReceived(message)

                    // إعادة بث الرسالة لجميع اللاعبين
                    broadcastMessage(message)
                }

            } catch (e: Exception) {
                removeClient(client)
            }
        }
    }

    /* ================= BROADCAST ================= */

    fun broadcastMessage(message: NetworkMessage) {
        val json = gson.toJson(message)

        clients.forEach { client ->
            try {
                client.output.writeUTF(json)
                client.output.flush()
            } catch (e: Exception) {
                removeClient(client)
            }
        }
    }

    /* ================= REMOVE CLIENT ================= */

    private fun removeClient(client: ClientConnection) {
        clients.remove(client)
        try { client.socket.close() } catch (_: Exception) {}

        println("Client removed: ${client.playerId}")
    }

    /* ================= STOP SERVER ================= */

    fun stopServer() {
        scope.cancel()

        clients.forEach {
            try { it.socket.close() } catch (_: Exception) {}
        }

        try { serverSocket?.close() } catch (_: Exception) {}

        println("Server stopped")
    }
}

/* ====================================================== */
/* ================= CLIENT CONNECTION =================== */
/* ====================================================== */

data class ClientConnection(
    val socket: Socket
) {
    val input: DataInputStream = DataInputStream(socket.inputStream)
    val output: DataOutputStream = DataOutputStream(socket.outputStream)
    val playerId: String = socket.inetAddress.hostAddress ?: "Unknown"
}

/* ====================================================== */
/* ================= NETWORK MESSAGE ===================== */
/* ====================================================== */

data class NetworkMessage(
    val playerId: String,
    val gameType: String,
    val action: String,
    val payload: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/* ====================================================== */
/* ================= NETWORK ACTIONS ===================== */
/* ====================================================== */

object NetworkActions {
    const val PLAYER_JOINED = "PLAYER_JOINED"
    const val PLAYER_LEFT = "PLAYER_LEFT"
    const val GAME_STARTED = "GAME_STARTED"
    const val GAME_STATE_UPDATE = "GAME_STATE_UPDATE"
    const val CARD_PLAYED = "CARD_PLAYED"
    const val TURN_CHANGED = "TURN_CHANGED"
    const val GAME_ENDED = "GAME_ENDED"
}
