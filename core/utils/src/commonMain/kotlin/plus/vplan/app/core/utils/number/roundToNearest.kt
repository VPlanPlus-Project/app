package plus.vplan.app.core.utils.number

import kotlin.math.abs

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
