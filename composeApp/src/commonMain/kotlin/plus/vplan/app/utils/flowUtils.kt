package plus.vplan.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

fun <T1, T2, T3, T4, T5, T6, R> combine6(
    f1: Flow<T1>,
    f2: Flow<T2>,
    f3: Flow<T3>,
    f4: Flow<T4>,
    f5: Flow<T5>,
    f6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return combine(
        combine(f1, f2, f3, f4, f5) { a, b, c, d, e ->
            Quintuple(a, b, c, d, e)
        },
        f6
    ) { q, f ->
        transform(q.first, q.second, q.third, q.fourth, q.fifth, f)
    }
}

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
