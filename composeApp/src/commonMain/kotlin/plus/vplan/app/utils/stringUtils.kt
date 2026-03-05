package plus.vplan.app.utils

const val DOT = "•"

/**
 * @return a lowercase hash of the context string
 */
expect fun String.sha256(): String

infix operator fun String.times(number: Int): String {
    if (number < 0) throw IllegalArgumentException("Number must not be smaller than zero")
    return this.repeat(number)
}

fun String.coerceLengthAtMost(length: Int, overflowChar: String = "…"): String {
    val result = this.take(length)
    if (this.length > length) return result + overflowChar
    return result
}