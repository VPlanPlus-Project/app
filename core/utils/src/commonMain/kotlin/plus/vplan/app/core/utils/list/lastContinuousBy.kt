package plus.vplan.app.core.utils.list

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