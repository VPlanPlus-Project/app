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