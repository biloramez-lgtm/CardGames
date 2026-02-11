package com.example.tasalicool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tasalicool.network.NetworkGameClient
import com.example.tasalicool.network.NetworkMessage
import com.example.tasalicool.network.NetworkActions

@Composable
fun JoinGameScreen(navController: NavHostController) {

    var serverIp by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<String>()) }

    val client = remember { NetworkGameClient() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "🔗 الانضمام إلى لعبة",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = serverIp,
            onValueChange = { serverIp = it },
            label = { Text("أدخل IP السيرفر") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (!isConnected && serverIp.isNotBlank()) {
                    client.connect(
                        hostIp = serverIp,
                        onConnected = {
                            isConnected = true
                            messages = messages + "✅ تم الاتصال بالسيرفر"
                        },
                        onMessageReceived = { message ->
                            messages = messages + "📩 ${message.action} من ${message.playerId}"
                        },
                        onDisconnected = {
                            isConnected = false
                            messages = messages + "❌ تم قطع الاتصال"
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isConnected) "متصل" else "اتصال")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isConnected) {
            Button(
                onClick = {
                    client.sendMessage(
                        NetworkMessage(
                            playerId = client.playerId,
                            gameType = "LOCAL_WIFI",
                            action = NetworkActions.GAME_STARTED
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إرسال رسالة تجريبية")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("السجل:")

        Spacer(modifier = Modifier.height(10.dp))

        messages.forEach {
            Text("• $it")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                client.disconnect()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("رجوع")
        }
    }
}
