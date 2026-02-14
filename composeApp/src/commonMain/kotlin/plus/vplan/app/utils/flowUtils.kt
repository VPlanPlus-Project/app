package plus.vplan.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Item

suspend fun <T> ProducerScope<T>.sendAll(flow: Flow<T>) {
    flow.takeWhile { this.isActive }.collectLatest { trySend(it) }
}

@Composable
fun <T: Item<*, *>> Flow<CacheState<T>>.getState(initialValue: T? = null) = this
    .filterIsInstance<CacheState.Done<T>>()
    .map { it.data }
    .collectAsState(initialValue)
