package com.example.tasalicool.ui.screens

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tasalicool.network.NetworkGameServer
import java.net.Inet4Address
import java.net.NetworkInterface

@Composable
fun HostGameScreen(navController: NavHostController) {

    var serverStarted by remember { mutableStateOf(false) }
    var connectedPlayers by remember { mutableStateOf(listOf<String>()) }

    val server = remember { NetworkGameServer(5000) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "🎮 استضافة لعبة",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "IP جهازك:",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = getLocalIpAddress() ?: "غير متصل بالشبكة",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (!serverStarted) {
                    server.startServer { playerId ->
                        connectedPlayers = connectedPlayers + playerId
                    }
                    serverStarted = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (serverStarted) "السيرفر يعمل..." else "تشغيل السيرفر")
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("اللاعبون المتصلون:")

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
/* 🔥 الحصول على IP الجهاز */
/* ============================= */

fun getLocalIpAddress(): String? {
    return try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            val addresses = intf.inetAddresses
            for (addr in addresses) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress
                }
            }
        }
        null
    } catch (ex: Exception) {
        null
    }
}
