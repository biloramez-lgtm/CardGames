import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*

@Composable
fun PlayerSideInfo(player: Player) {
    Text(
        text = "${player.name}\nScore: ${player.score}",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun PlayerVerticalInfo(player: Player) {
    Column {
        Text(player.name)
        Text("Score: ${player.score}")
        Text("Bid: ${player.bid}")
    }
}
