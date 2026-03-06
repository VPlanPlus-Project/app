package plus.vplan.app.feature.onboarding.utils

import androidx.compose.ui.graphics.Color

fun blendColor(color1: Color, color2: Color, factor: Float): Color {
    val red = (1 - factor) * color1.red + factor * color2.red
    val green = (1 - factor) * color1.green + factor * color2.green
    val blue = (1 - factor) * color1.blue + factor * color2.blue
    val alpha = (1 - factor) * color1.alpha + factor * color2.alpha

    return Color(red, green, blue, alpha)
}