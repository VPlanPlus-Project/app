package plus.vplan.app.domain.cache

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job

open class IdentifiedValue <K, V>(private val onDelete: (V) -> Unit) {
    private var key: K? = null
    private var value: V? = null

    fun setOnNewKey(key: K, value: (K) -> V) {
        if (key == this.key) return
        Logger.d { "Restart $key" }
        this.value?.let { onDelete(it) }
        this.key = key
        this.value = value(key)
    }

    fun set(key: K, value: V) {
        this.value?.let(onDelete)
        this.key = key
        this.value = value
    }
}

class IdentifiedJob<K> : IdentifiedValue<K, Job>(onDelete = { it.cancel() })