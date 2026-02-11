package com.example.tasalicool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "🃏 tasalicool",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ألعاب الورق العربية",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(36.dp))

        GameCard(
            title = "لعبة 400",
            description = "للاعبين 2 - 4",
            icon = "🎴",
            onClick = { navController.navigate("game_400") }
        )

        GameCard(
            title = "Solitaire",
            description = "لعبة فردية",
            icon = "🎯",
            onClick = { navController.navigate("solitaire") }
        )

        GameCard(
            title = "Hand Game",
            description = "لعبة متعددة اللاعبين",
            icon = "🤝",
            onClick = { navController.navigate("hand_game") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = { navController.navigate("bluetooth") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Bluetooth")
            }

            Button(
                onClick = { navController.navigate("network") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Network")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GameCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = icon,
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ابدأ اللعب")
            }
        }
    }
}
