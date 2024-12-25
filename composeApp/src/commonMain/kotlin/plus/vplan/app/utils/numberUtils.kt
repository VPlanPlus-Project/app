package plus.vplan.app.utils

fun List<Int>.isContinuous(): Boolean {
    if (isEmpty()) return true
    var last = first()
    drop(1).forEach { current ->
        if (current != last + 1) return false
        last = current
    }
    return true
}

/**
 * Returns the last element that is conditionally continuous with the previous element.
 * If the list is empty, returns null. If all elements are continuous, returns null.
 */
fun <T> List<T>.lastContinuousBy(predicate: (T) -> Int): T? {
    if (size < 2) return null
    val array = sortedBy { predicate(it) }
    var last = array.first()
    array.drop(1).forEach { current ->
        if (predicate(current) != predicate(last) + 1) return last
        last = current
    }
    return null
}

fun <T> List<T>.takeContinuousBy(predicate: (T) -> Int): List<T> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<T>()
    result.add(first())
    drop(1).forEach { current ->
        if (predicate(current) != predicate(result.last()) + 1) return result
        result.add(current)
    }
    return result
}