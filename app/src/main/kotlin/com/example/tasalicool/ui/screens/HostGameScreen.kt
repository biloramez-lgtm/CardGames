package com.example.tasalicool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tasalicool.models.Game400Engine
import com.example.tasalicool.network.NetworkGameServer
import java.net.Inet4Address
import java.net.NetworkInterface

@Composable
fun HostGameScreen(
    navController: NavHostController,
    gameEngine: Game400Engine
) {

    var serverStarted by remember { mutableStateOf(false) }
    var connectedPlayers by remember { mutableStateOf(listOf<String>()) }
    var statusText by remember { mutableStateOf("السيرفر غير مشغل") }

    val server = remember { NetworkGameServer(5000, gameEngine) }

    DisposableEffect(Unit) {
        onDispose {
            server.stopServer()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "🎮 استضافة لعبة عبر Wi-Fi",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "IP جهازك:",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = getWifiIpAddress() ?: "غير متصل بالشبكة",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(statusText)

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {

                if (!serverStarted) {

                    server.startServer(
                        onClientConnected = { playerId ->
                            connectedPlayers =
                                connectedPlayers + playerId
                            statusText = "لاعب متصل: $playerId"
                        },
                        onClientDisconnected = { playerId ->
                            connectedPlayers =
                                connectedPlayers.filter { it != playerId }
                            statusText = "لاعب قطع الاتصال: $playerId"
                        },
                        onGameUpdated = {
                            statusText = "تم تحديث حالة اللعبة"
                        }
                    )

                    serverStarted = true
                    statusText = "السيرفر يعمل على المنفذ 5000"
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (serverStarted)
                    "السيرفر يعمل..."
                else
                    "تشغيل السيرفر"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (serverStarted) {
            Button(
                onClick = {
                    server.stopServer()
                    connectedPlayers = emptyList()
                    serverStarted = false
                    statusText = "تم إيقاف السيرفر"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("إيقاف السيرفر")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("اللاعبون المتصلون:")

        Spacer(modifier = Modifier.height(10.dp))

        connectedPlayers.forEach {
            Text("• $it")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("رجوع")
        }
    }
}

/* ============================= */
/* الحصول على IP WiFi الصحيح */
/* ============================= */

fun getWifiIpAddress(): String? {
    return try {
        NetworkInterface.getNetworkInterfaces().toList().forEach { intf ->
            if (intf.name.contains("wlan", true)) {
                intf.inetAddresses.toList().forEach { addr ->
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        }
        null
    } catch (e: Exception) {
        null
    }
}
