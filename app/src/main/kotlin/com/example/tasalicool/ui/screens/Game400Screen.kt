package com.example.tasalicool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tasalicool.models.Card
import com.example.tasalicool.models.Game400Round
import com.example.tasalicool.models.Player
import com.example.tasalicool.ui.components.CardBackView
import com.example.tasalicool.ui.components.CardView
import com.example.tasalicool.ui.components.CompactCardView

@Composable
fun Game400Screen(navController: NavHostController) {

    var gameRound by remember {
        mutableStateOf(
            Game400Round(
                players = listOf(
                    Player("p1", "أنت"),
                    Player("p2", "اللاعب 2")
                )
            )
        )
    }

    var selectedCard by remember { mutableStateOf<Card?>(null) }

    // 🔥 تهيئة مرة واحدة فقط
    LaunchedEffect(Unit) {
        gameRound.initialize()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "🎴 لعبة 400",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // معلومات اللاعبين
        gameRound.players.forEach { player ->
            PlayerInfoCard(
                player = player,
                isCurrentPlayer = player == gameRound.getCurrentPlayer()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // الطاولة
        Text(
            text = "أوراق على الطاولة",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (gameRound.deck.size() > 0) {
                CardBackView(
                    onClick = {
                        gameRound.drawFromDeck(gameRound.getCurrentPlayer())
                    }
                )
                Text(text = "${gameRound.deck.size()}")
            }

            if (gameRound.discardPile.isNotEmpty()) {
                CardView(card = gameRound.discardPile.last())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // يد اللاعب
        Text(
            text = "أوراقك",
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(gameRound.getCurrentPlayer().hand) { card ->
                CompactCardView(
                    card = card,
                    isSelected = card == selectedCard,
                    onClick = { selectedCard = card }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // أزرار الحركة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = {
                    selectedCard?.let {
                        if (gameRound.canPlay(it)) {
                            gameRound.playCard(it)
                            selectedCard = null
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = selectedCard != null
            ) {
                Text("لعب الورقة")
            }

            Button(
                onClick = {
                    gameRound.drawFromDeck(gameRound.getCurrentPlayer())
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("سحب ورقة")
            }
        }
    }
}

@Composable
fun PlayerInfoCard(player: Player, isCurrentPlayer: Boolean) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    text = if (isCurrentPlayer) "▶ ${player.name}" else player.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "عدد الأوراق: ${player.handSize()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "${player.score} نقطة",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
