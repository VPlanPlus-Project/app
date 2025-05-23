package plus.vplan.app.domain.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import plus.vplan.app.domain.data.Response

sealed class CacheState<out T: Item<*>>(val entityId: String) {
    data class Loading(val id: String, val source: Source? = null): CacheState<Nothing>(id) {
        enum class Source {
            Network, Local
        }
    }
    data class NotExisting(val id: String): CacheState<Nothing>(id)
    data class Error(val id: String, val error: Response.Error): CacheState<Nothing>(id) {
        constructor(id: Int, error: Response.Error): this(id.toString(), error)
    }
    data class Done<T: Item<*>>(val data: T): CacheState<T>(data.getEntityId())
}

interface Item<T: DataTag> {
    fun getEntityId(): String
    val tags: Set<T>
}

/**
 * Tags that can be used to indicate how much data is already loaded for an entity.
 */
interface DataTag

suspend inline fun <reified T : Item<*>> Flow<CacheState<T>>.getFirstValue(vararg requiredTags: DataTag): T? {
    return this
        .onEach { if (it is CacheState.Error) Logger.e { "Failed to load entity ${it.entityId}: ${it.error}" } }
        .filter { it is CacheState.NotExisting || it is CacheState.Done || it is CacheState.Error }
        .map {
            if (it is CacheState.Done && it.data.tags.all { it in requiredTags }) it.data
            else null
        }
        .onEmpty { throw RuntimeException("Failed to load entity ${T::class.simpleName}, required tags: $requiredTags, Cause: Flow was empty") }
        .catch {
            throw RuntimeException("Failed to load entity ${T::class.simpleName}, required tags: $requiredTags", it)
        }
        .first()
}

@Composable
fun <T: Item<*>> Flow<CacheState<T>>.collectAsLoadingState(id: String = "Unbekannt") = this.collectAsState(CacheState.Loading(id))

@Composable
fun <T: Item<*>> Flow<CacheState<T>>.collectAsResultingFlow() = this.filterIsInstance<CacheState.Done<T>>().map { it.data }.collectAsState(null)

@Composable
inline fun <reified T: Item<*>> List<Flow<CacheState<T>>>.collectAsResultingFlow(): State<List<T>> = if (this.isEmpty()) produceState(emptyList()) {} else (combine(this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }) { it.toList() }.collectAsState(emptyList()))

@Composable
inline fun <reified T: Item<*>> Flow<List<CacheState<T>>>.collectAsSingleFlow(): State<List<T>> = this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }.distinctUntilChanged().collectAsState(emptyList())