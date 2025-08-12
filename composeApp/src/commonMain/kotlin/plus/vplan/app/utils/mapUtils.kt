package plus.vplan.app.utils

fun <K, V> Map<K?, V>.filterKeysNotNull(): Map<K, V> {
    return this.filterKeys { it != null }.mapKeys { it.key!! }
}