package com.example.tasalicool.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasalicool.models.Card

/* =========================================================
   🎴 UTILITIES
   ========================================================= */

private fun Card.suitSymbol(): String =
    suit.displayName.first().toString()

private fun Card.suitColor(): Color =
    if (suit.displayName.contains("♥") || suit.displayName.contains("♦"))
        Color(0xFFD32F2F)
    else
        Color(0xFF212121)

/* =========================================================
   🎴 MAIN CARD FRONT
   ========================================================= */

@Composable
fun CardView(
    card: Card,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {

    val backgroundColor by animateColorAsState(
        if (isSelected) Color(0xFFFFF8E1) else Color.White,
        label = ""
    )

    val elevation by animateDpAsState(
        if (isSelected) 14.dp else 6.dp,
        label = ""
    )

    val scale by animateFloatAsState(
        if (isSelected) 1.07f else 1f,
        label = ""
    )

    Box(
        modifier = modifier
            .size(80.dp, 120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (isSelected) -12f else 0f
            }
            .shadow(elevation, RoundedCornerShape(12.dp))
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(
                if (isSelected) 3.dp else 1.dp,
                if (isSelected) Color(0xFFFFA000) else Color(0xFFBDBDBD),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled && onClick != null) {
                onClick?.invoke()
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Corner(card.rank.displayName, card.suitSymbol(), card.suitColor())

            Text(
                text = card.suitSymbol(),
                fontSize = 32.sp,
                color = card.suitColor(),
                fontWeight = FontWeight.Bold
            )

            Corner(card.suitSymbol(), card.rank.displayName, card.suitColor())
        }
    }
}

/* ========================================================= */

@Composable
private fun Corner(
    top: String,
    bottom: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = top,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = bottom,
            fontSize = 14.sp,
            color = color
        )
    }
}

/* =========================================================
   🎴 CARD BACK
   ========================================================= */

@Composable
fun CardBackView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp, 120.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .background(Color(0xFF1A237E), RoundedCornerShape(12.dp))
            .border(
                2.dp,
                Color(0xFF0D47A1),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "♠♥♦♣",
            fontSize = 24.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/* =========================================================
   🎴 COMPACT CARD (HAND)
   ========================================================= */

@Composable
fun CompactCardView(
    card: Card,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {

    val elevation by animateDpAsState(
        if (isSelected) 10.dp else 3.dp,
        label = ""
    )

    val scale by animateFloatAsState(
        if (isSelected) 1.05f else 1f,
        label = ""
    )

    Box(
        modifier = modifier
            .size(55.dp, 75.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = if (isSelected) -8f else 0f
            }
            .shadow(elevation, RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFFFFF8E1) else Color.White,
                RoundedCornerShape(8.dp)
            )
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) Color(0xFFFFA000) else Color(0xFFBDBDBD),
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled && onClick != null) {
                onClick?.invoke()
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = card.rank.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = card.suitColor()
            )
            Text(
                text = card.suitSymbol(),
                fontSize = 15.sp,
                color = card.suitColor()
            )
        }
    }
}

/* =========================================================
   🎴 3D FLIP CARD
   ========================================================= */

@Composable
fun FlipCardView(
    card: Card,
    modifier: Modifier = Modifier,
    isFaceUp: Boolean,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {

    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = tween(400),
        label = ""
    )

    Box(
        modifier = modifier
            .size(80.dp, 120.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {

        if (rotation <= 90f) {
            CardBackView(Modifier.matchParentSize())
        } else {
            CardView(
                card = card,
                isSelected = isSelected,
                enabled = enabled,
                onClick = onClick,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            )
        }
    }
}
