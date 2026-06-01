package de.hochschulz.osmandhome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.hochschulz.osmandhome.util.ColorAssigner

@Composable
fun EntityAvatar(
    name: String,
    entityId: String,
    size: Dp = 48.dp,
    fontSize: TextUnit = 16.sp
) {
    val color = ColorAssigner.forEntity(entityId)
    val initials = name.split(" ").take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { name.take(2).uppercase() }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = initials,
            color      = Color.White,
            fontSize   = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}