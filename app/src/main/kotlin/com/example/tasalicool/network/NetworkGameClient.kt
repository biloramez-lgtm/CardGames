package com.example.tasalicool.network

import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class NetworkGameClient {

    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var playerId: String = "Player_${System.currentTimeMillis()}"

    /* ================= CONNECT ================= */

    fun connect(
        hostIp: String,
        port: Int = 5000,
        onConnected: () -> Unit = {},
        onMessageReceived: (NetworkMessage) -> Unit = {},
        onDisconnected: () -> Unit = {}
    ) {
        scope.launch {
            try {
                socket = Socket(hostIp, port)
                input = DataInputStream(socket!!.inputStream)
                output = DataOutputStream(socket!!.outputStream)

                onConnected()

                listen(onMessageReceived, onDisconnected)

                // إرسال رسالة انضمام
                sendMessage(
                    NetworkMessage(
                        playerId = playerId,
                        gameType = "LOCAL_WIFI",
                        action = NetworkActions.PLAYER_JOINED
                    )
                )

            } catch (e: Exception) {
                onDisconnected()
            }
        }
    }

    /* ================= LISTEN ================= */

    private fun listen(
        onMessageReceived: (NetworkMessage) -> Unit,
        onDisconnected: () -> Unit
    ) {
        scope.launch {
            try {
                while (isActive) {
                    val json = input?.readUTF() ?: break
                    val message =
                        gson.fromJson(json, NetworkMessage::class.java)

                    onMessageReceived(message)
                }
            } catch (e: Exception) {
                onDisconnected()
            }
        }
    }

    /* ================= SEND ================= */

    fun sendMessage(message: NetworkMessage) {
        scope.launch {
            try {
                val json = gson.toJson(message)
                output?.writeUTF(json)
                output?.flush()
            } catch (_: Exception) {}
        }
    }

    /* ================= DISCONNECT ================= */

    fun disconnect() {
        try {
            sendMessage(
                NetworkMessage(
                    playerId = playerId,
                    gameType = "LOCAL_WIFI",
                    action = NetworkActions.PLAYER_LEFT
                )
            )
        } catch (_: Exception) {}

        scope.cancel()

        try { socket?.close() } catch (_: Exception) {}
    }
}
