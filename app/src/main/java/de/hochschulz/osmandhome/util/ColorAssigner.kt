package de.hochschulz.osmandhome.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs

object ColorAssigner {
    private val palette = listOf(
        Color(0xFF01696F), Color(0xFF437A22), Color(0xFFa12c7b),
        Color(0xFFda7101), Color(0xFF006494), Color(0xFFd19900),
        Color(0xFFa13544), Color(0xFF7a39bb)
    )

    fun forEntity(entityId: String): Color  = palette[abs(entityId.hashCode()) % palette.size]
    fun forEntityArgb(entityId: String): Int = forEntity(entityId).toArgb()
}