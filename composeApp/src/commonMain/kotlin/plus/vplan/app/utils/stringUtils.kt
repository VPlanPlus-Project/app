package plus.vplan.app.utils

const val DOT = "•"

fun String.removeFollowingDuplicates(
    vararg chars: Char
): String {
    if (isEmpty()) return this

    val result = StringBuilder()
    var previousChar: Char? = null

    for (currentChar in this) {
        if (!(currentChar == previousChar && chars.contains(currentChar))) {
            result.append(currentChar)
        }
        previousChar = currentChar
    }

    return result.toString()
}

fun String.splitWithKnownValuesBySpace(values: List<String>): List<String> {
    val regex = Regex(values.joinToString("|") { Regex.escape(it) })
    val matches = mutableListOf<String>()
    var remaining = this
    while (true) {
        val match = regex.find(remaining) ?: break
        matches.add(match.value)
        remaining = remaining.removeRange(match.range).trim()
    }

    return if (remaining.isEmpty()) matches else emptyList()
}

expect fun String.sha256(): String

infix operator fun String.times(number: Int): String {
    if (number < 0) throw IllegalArgumentException("Number must not be smaller than zero")
    return this.repeat(number)
}