package com.example.tasalicool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tasalicool.models.*
import com.example.tasalicool.R

@Composable
fun Game400Screen(
    navController: NavHostController,
    gameEngine: Game400Engine
) {

    val engine = gameEngine

    LaunchedEffect(Unit) {
        engine.startGame()
    }

    if (engine.players.size < 4) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val localPlayer = engine.players[0]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E3B2E))
    ) {

        when (engine.phase) {

            /* ================= BIDDING ================= */

            GamePhase.BIDDING -> {

                if (engine.getCurrentPlayer() == localPlayer) {

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = stringResource(R.string.choose_bid),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(16.dp))

                        LazyRow {
                            items((2..13).toList()) { bid ->
                                Button(
                                    onClick = {
                                        engine.placeBid(localPlayer, bid)
                                    },
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(text = bid.toString())
                                }
                            }
                        }
                    }
                } else {

                    Box(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.waiting_bid,
                                engine.getCurrentPlayer().name
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            /* ================= PLAYING ================= */

            GamePhase.PLAYING -> {

                val leftPlayer = engine.players[1]
                val topPlayer = engine.players[2]
                val rightPlayer = engine.players[3]

                // Player names around table
                Text(
                    text = topPlayer.name,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                Text(
                    text = leftPlayer.name,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = rightPlayer.name,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )

                Text(
                    text = localPlayer.name,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Current trick (center)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    engine.currentTrick.forEach { (player, card) ->
                        Text(
                            text = "${player.name}: ${card.rank} ${card.suit}",
                            color = Color.White
                        )
                    }
                }

                // Local player hand
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                ) {
                    items(localPlayer.hand) { card ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    engine.playCard(localPlayer, card)
                                }
                        ) {
                            Text(
                                text = "${card.rank} ${card.suit}",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            /* ================= GAME OVER ================= */

            GamePhase.GAME_OVER -> {
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = stringResource(
                            R.string.winner_text,
                            engine.winner?.name ?: ""
                        ),
                        color = Color.Yellow,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            else -> {}
        }
    }
}
