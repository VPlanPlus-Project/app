package plus.vplan.app.utils

suspend inline fun <T, R : Comparable<R>> Iterable<T>.sortedBySuspending(
    crossinline selector: suspend (T) -> R?
): List<T> = this
    .map { it to selector(it) }
    .sortedBy { it.second }
    .map { it.first }