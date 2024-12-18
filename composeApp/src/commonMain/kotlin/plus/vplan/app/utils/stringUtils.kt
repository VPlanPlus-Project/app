package plus.vplan.app.utils

fun String.removeFollowingDuplicates(
    chars: List<Char>
): String {
    if (isEmpty()) return this

    val result = StringBuilder()
    var previousChar: Char? = null

    for (currentChar in this) {
        if (currentChar != previousChar && !chars.contains(currentChar)) {
            result.append(currentChar)
        }
        previousChar = currentChar
    }

    return result.toString()
}