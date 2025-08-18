package plus.vplan.app.utils

fun <K, V> Map<K?, V>.filterKeysNotNull(): Map<K, V> {
    return this.filterKeys { it != null }.mapKeys { it.key!! }
}


/**
 * Sorts a map by its keys. The keys must be [Comparable].
 */
fun <K, V> Map<K, V>.sortedByKey(): Map<K, V> where K : Comparable<K> =
    this.toList().sortedBy { it.first }.toMap()

suspend fun <T, V : Any> Collection<T>.associateWithNotNull(
    selector: suspend (T) -> V?
): Map<T, V> {
    return this.mapNotNull { item ->
        val value = selector(item)
        if (value != null) item to value else null
    }.toMap()
}