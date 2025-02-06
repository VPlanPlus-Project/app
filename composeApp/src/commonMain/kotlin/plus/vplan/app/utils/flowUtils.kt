package plus.vplan.app.utils

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive

suspend fun <T> Flow<T>.latest(): T = this.first()

suspend fun <T> ProducerScope<T>.sendAll(flow: Flow<T>) {
    flow.takeWhile { this.isActive }.collectLatest { trySend(it) }
}