package plus.vplan.app.domain.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import plus.vplan.app.domain.data.Response

sealed class CacheState<out T: Item<*>>(val entityId: String) {
    data class Loading(val id: String): CacheState<Nothing>(id)
    data class NotExisting(val id: String): CacheState<Nothing>(id)
    data class Error(val id: String, val error: Response.Error): CacheState<Nothing>(id)
    data class Done<T: Item<*>>(val data: T): CacheState<T>(data.getEntityId())
}

interface Item<T: DataTag> {
    fun getEntityId(): String
}

interface DataTag

suspend fun <T: Item<*>> Flow<CacheState<T>>.getFirstValue() = (this.onEach { if (it is CacheState.Error) Logger.e {
    "Failed to load entity ${it.entityId}: ${it.error}"
} }.filter { it is CacheState.NotExisting || it is CacheState.Done || it is CacheState.Error }.first() as? CacheState.Done<T>)?.data

@Composable
fun <T: Item<*>> Flow<CacheState<T>>.collectAsLoadingState(id: String = "Unbekannt") = this.collectAsState(CacheState.Loading(id))

@Composable
fun <T: Item<*>> Flow<CacheState<T>>.collectAsResultingFlow() = this.filterIsInstance<CacheState.Done<T>>().map { it.data }.collectAsState(null)

@Composable
inline fun <reified T: Item<*>> List<Flow<CacheState<T>>>.collectAsResultingFlow(): State<List<T>> = if (this.isEmpty()) produceState(emptyList()) {} else (combine(this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }) { it.toList() }.collectAsState(emptyList()))

@Composable
inline fun <reified T: Item<*>> Flow<List<CacheState<T>>>.collectAsSingleFlow(): State<List<T>> = this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }.distinctUntilChanged().collectAsState(emptyList())