package plus.vplan.app.utils

/**
 * @param lambda A lambda to return a boolean value, if true gets returned, the function will exit and skip the remaining records
 */
inline fun <T> Collection<T>.forEachBreakable(lambda: (T) -> Boolean) {
    this.forEach {
        if (lambda(it)) return
    }
}