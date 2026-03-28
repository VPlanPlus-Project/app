package plus.vplan.app.utils

import androidx.compose.ui.graphics.Color

fun String.generateColor(): Color {
    var hash = 0
    for (char in this) {
        hash = char.code + ((hash shl 5) - hash)
    }

    val hue = (hash % 360 + 360) % 360
    val saturation = 0.9f
    val lightness = 0.5f

    return Color.hsl(hue.toFloat(), saturation, lightness)
}
