package plus.vplan.app.core.utils.number

import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToLong

fun Long.toHumanSize(): String {
    val units = listOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    if (this < 1024) return "$this ${units[0]}"

    val exponent = (log(this.toDouble(), 2.0) / log(1024.0, 2.0)).toInt()
    val unit = units.getOrElse(exponent) { "Too Large" }
    val value = this / 1024.0.pow(exponent.toDouble())

    return "${((value * 10).roundToLong() / 10)} $unit"
}
