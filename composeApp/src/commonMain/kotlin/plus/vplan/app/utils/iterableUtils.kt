package plus.vplan.app.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend inline fun <T, R : Comparable<R>> Iterable<T>.sortedBySuspending(
    crossinline selector: suspend (T) -> R?
): List<T> = this
    .map { it to selector(it) }
    .sortedBy { it.second }
    .map { it.first }

suspend fun <K, V, R : Comparable<R>> Map<K, V>.sortedBySuspending(
    selector: suspend (Map.Entry<K, V>) -> R?
): List<Map.Entry<K, V>> = coroutineScope {
    this@sortedBySuspending
        .entries
        .map { entry -> async { entry to selector(entry) } }
        .awaitAll()
        .sortedBy { it.second }
        .map { it.first }
}