@file:Suppress("unused")

package plus.vplan.app.utils


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
