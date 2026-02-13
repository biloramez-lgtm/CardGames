package com.example.tasalicool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.tasalicool.models.Game400Engine
import com.example.tasalicool.models.GameMode
import com.example.tasalicool.ui.screens.*
import com.example.tasalicool.ui.theme.TasalicoolTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TasalicoolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // 🔥 محرك لعبة واحد مشترك
                    val gameEngine = remember {
                        Game400Engine(
                            gameMode = GameMode.SINGLE_PLAYER
                        )
                    }

                    TasalicoolNavGraph(
                        navController = navController,
                        engine = gameEngine
                    )
                }
            }
        }
    }
}

/* ========================================================= */
/* ================= NAVIGATION GRAPH ====================== */
/* ========================================================= */

@Composable
fun TasalicoolNavGraph(
    navController: NavHostController,
    engine: Game400Engine
) {

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(navController)
        }

        // 🔥 شاشة لعبة 400 الجديدة مع الطاولة الخضراء
        composable("game_400") {

            // بدء لعبة جديدة عند الدخول
            engine.startGame()

            GameTableScreen(
                navController = navController,
                engine = engine
            )
        }

        // 🔥 استكمال نفس المحرك
        composable("resume_game") {

            GameTableScreen(
                navController = navController,
                engine = engine
            )
        }

        composable("about") {
            AboutScreen(navController)
        }

        composable("host_game") {
            HostGameScreen(navController)
        }

        composable("join_game") {
            JoinGameScreen(navController)
        }

        composable("solitaire") {
            PlaceholderScreen("Solitaire", navController)
        }

        composable("hand_game") {
            PlaceholderScreen("Hand Game", navController)
        }
    }
}

/* ========================================================= */
/* ================= PLACEHOLDER SCREEN ==================== */
/* ========================================================= */

@Composable
fun PlaceholderScreen(
    title: String,
    navController: NavHostController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "🚧",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$title\nقريباً...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            ) {
                Text("العودة للرئيسية")
            }
        }
    }
}
