package plus.vplan.app.utils

fun <K, V> Map<K?, V>.filterKeysNotNull(): Map<K, V> {
    return this.filterKeys { it != null }.mapKeys { it.key!! }
}

suspend fun <T, V : Any> Collection<T>.associateWithNotNull(
    selector: suspend (T) -> V?
): Map<T, V> {
    return this.mapNotNull { item ->
        val value = selector(item)
        if (value != null) item to value else null
    }.toMap()
}