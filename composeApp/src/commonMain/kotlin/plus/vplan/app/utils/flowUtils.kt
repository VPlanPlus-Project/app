package plus.vplan.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.Item

suspend fun <T> Flow<T>.latest(): T = this.warnOnTimeoutAfter10Seconds().first()

suspend fun <T> ProducerScope<T>.sendAll(flow: Flow<T>) {
    flow.takeWhile { this.isActive }.collectLatest { trySend(it) }
}

@Composable
fun <T: Item<*>> Flow<CacheState<T>>.getState(initialValue: T? = null) = this
    .filterIsInstance<CacheState.Done<T>>()
    .map { it.data }
    .collectAsState(initialValue)

fun <T: Any?> Flow<T>.warnOnTimeoutAfter10Seconds(): Flow<T> {
    var hasEmitted = false
    return this
        .onStart {
            delay(10 * 1000)
            if (hasEmitted) return@onStart
            Logger.w { "Flow $this has been waiting for 10 seconds without emitting something" }
        }
        .onEach { hasEmitted = false }
}