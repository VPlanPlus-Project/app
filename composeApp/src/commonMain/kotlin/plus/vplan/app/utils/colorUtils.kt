package plus.vplan.app.utils

import androidx.compose.ui.graphics.Color

fun Color.toBlackAndWhite(): Color {
    val r = this.red/255
    val g = this.green/255
    val b = this.blue/255
    val cMax = listOf(r, g, b).max()
    val cMin = listOf(r, g, b).min()
    val l = (cMin + cMax)/2

    val h =
        if (cMin == cMax) 0f
        else if (cMax == r) 60*(g-b)/(cMax-cMin)
        else if (cMax == g) 60*(2+(b-r)/(cMax-cMin))
        else 60*(4+(r-g)/(cMax-cMin))

    val color = Color.hsl(h, 0f, l, this.alpha)
    return color
}

fun Color.transparent(alpha: Float = 0f) = this.copy(alpha = alpha)

fun blendColor(color1: Color, color2: Color, factor: Float): Color {
    val red = (1 - factor) * color1.red + factor * color2.red
    val green = (1 - factor) * color1.green + factor * color2.green
    val blue = (1 - factor) * color1.blue + factor * color2.blue
    val alpha = (1 - factor) * color1.alpha + factor * color2.alpha

    return Color(red, green, blue, alpha)
}