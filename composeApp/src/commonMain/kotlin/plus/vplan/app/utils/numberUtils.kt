@file:Suppress("unused")

package plus.vplan.app.utils

import kotlin.math.abs
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToLong


inline fun <T> List<T>.takeContinuousBy(predicate: (T) -> Int): List<T> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<T>()
    result.add(first())
    drop(1).forEach { current ->
        if (predicate(current) != predicate(result.last()) + 1) return result
        result.add(current)
    }
    return result
}

inline infix fun <reified T: Number> Number.roundToNearest(numbers: List<T>): T {
    require(numbers.isNotEmpty())
    return numbers.minBy { abs(it.toDouble() - this.toDouble()) }.let {
        when (T::class) {
            Int::class -> it.toInt()
            Long::class -> it.toLong()
            Float::class -> it.toFloat()
            Double::class -> it.toDouble()
            else -> throw IllegalStateException("Unknown number type")
        } as T
    }
}

inline fun <reified T: Number> T.ifNan(block: () -> T): T {
    return when (this) {
        is Float -> if (this.isNaN()) block() else this
        is Double -> if (this.isNaN()) block() else this
        else -> this
    }
}

fun Long.toHumanSize(): String {
    val units = listOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    if (this < 1024) return "$this ${units[0]}"

    val exponent = (log(this.toDouble(), 2.0) / log(1024.0, 2.0)).toInt()
    val unit = units.getOrElse(exponent) { "Too Large" }
    val value = this / 1024.0.pow(exponent.toDouble())

    return "${((value * 10).roundToLong() / 10)} $unit"
}
