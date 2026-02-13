package com.example.tasalicool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasalicool.network.NetworkGameClient
import com.example.tasalicool.viewmodel.GameViewModel

@Composable
fun JoinGameScreen(
    navController: NavHostController,
    viewModel: GameViewModel = viewModel()
) {

    val gameEngine = viewModel.engine

    var ipAddress by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("🔴 Not Connected") }
    var connected by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    val client = remember { NetworkGameClient(gameEngine) }

    LaunchedEffect(Unit) {
        gameEngine.isNetworkClient = true
    }

    LaunchedEffect(Unit) {
        client.onGameStarted = {
            if (!hasNavigated) {
                hasNavigated = true
                navController.navigate("game_400") {
                    popUpTo("join_game") { inclusive = true }
                }
            }
        }

        client.onStateSynced = {
            gameEngine.onGameUpdated?.invoke()
        }
    }

    DisposableEffect(Unit) {
        onDispose { client.disconnect() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "🎮 Multiplayer Lobby",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(modifier = Modifier.padding(18.dp)) {

                Text(
                    text = "🔗 Connect to Host",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("Host IP Address") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !connected,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = statusText,
                    color = if (connected)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFFF44336)
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (!connected) {

                    Button(
                        onClick = {
                            client.connect(
                                hostIp = ipAddress,
                                port = 5000,
                                onConnected = {
                                    connected = true
                                    statusText = "🟢 Connected to Host"
                                    client.requestSync()
                                },
                                onDisconnected = {
                                    connected = false
                                    ready = false
                                    hasNavigated = false
                                    statusText = "🔴 Disconnected"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = ipAddress.isNotBlank()
                    ) {
                        Text("Connect")
                    }

                } else {

                    Button(
                        onClick = {
                            client.disconnect()
                            connected = false
                            ready = false
                            hasNavigated = false
                            statusText = "🔴 Disconnected"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Disconnect")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
