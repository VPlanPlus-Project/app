package plus.vplan.app.utils

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend fun <T> Flow<T>.latest(): T = this.first()

suspend fun <T> ProducerScope<T>.sendAll(flow: Flow<T>) {
    flow.collect { this.send(it) }
}