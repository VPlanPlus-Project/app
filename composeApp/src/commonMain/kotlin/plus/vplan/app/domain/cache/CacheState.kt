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
import plus.vplan.app.domain.data.AliasedItem
import plus.vplan.app.domain.data.Response

sealed class CacheStateOld<out T: Item<*>>(val entityId: String) {
    data class Loading(val id: String, val source: CacheState.Loading.Source? = null): CacheStateOld<Nothing>(id) {
        enum class Source {
            Network, Local
        }
    }
    data class NotExisting(val id: String): CacheStateOld<Nothing>(id)
    data class Error(val id: String, val error: Response.Error): CacheStateOld<Nothing>(id) {
        constructor(id: Int, error: Response.Error): this(id.toString(), error)
    }
    data class Done<T: Item<*>>(val data: T): CacheStateOld<T>(data.getEntityId())
}

sealed class CacheState<out T: AliasedItem<*>>(val entityId: String) {
    data class Loading(val id: String, val source: Source? = null): CacheState<Nothing>(id) {
        enum class Source {
            Network, Local
        }
    }
    data class NotExisting(val id: String): CacheState<Nothing>(id)
    data class Error(val id: String, val error: Response.Error): CacheState<Nothing>(id) {
        constructor(id: Int, error: Response.Error): this(id.toString(), error)
    }
    data class Done<T: AliasedItem<*>>(val data: T): CacheState<T>(data.id.toHexString())
}

interface Item<T: DataTag> {
    fun getEntityId(): String
    val tags: Set<T>
}

/**
 * Tags that can be used to indicate how much data is already loaded for an entity.
 */
interface DataTag

suspend inline fun <reified T : Item<*>> Flow<CacheStateOld<T>>.getFirstValueOld(vararg requiredTags: DataTag): T? {
    return this
        .onEach { if (it is CacheStateOld.Error) Logger.e { "Failed to load entity ${it.entityId}: ${it.error}" } }
        .filter { it is CacheStateOld.NotExisting || it is CacheStateOld.Done || it is CacheStateOld.Error }
        .map {
            if (it is CacheStateOld.Done && it.data.tags.all { it in requiredTags }) it.data
            else null
        }
        .onEmpty { throw RuntimeException("Failed to load entity ${T::class.simpleName}, required tags: $requiredTags, Cause: Flow was empty") }
        .catch {
            throw RuntimeException("Failed to load entity ${T::class.simpleName}, required tags: $requiredTags", it)
        }
        .first()
}

suspend inline fun <reified T : AliasedItem<*>> Flow<CacheState<T>>.getFirstValue(vararg requiredTags: DataTag): T? {
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
fun <T: Item<*>> Flow<CacheStateOld<T>>.collectAsLoadingStateOld(id: String = "Unbekannt") = this.collectAsState(CacheStateOld.Loading(id))

@Composable
fun <T: Item<*>> Flow<CacheStateOld<T>>.collectAsResultingFlowOld() = this.filterIsInstance<CacheStateOld.Done<T>>().map { it.data }.collectAsState(null)

@Composable
inline fun <reified T: Item<*>> List<Flow<CacheStateOld<T>>>.collectAsResultingFlowOld(): State<List<T>> = if (this.isEmpty()) produceState(emptyList()) {} else (combine(this.map { it.filterIsInstance<CacheStateOld.Done<T>>().map { it.data } }) { it.toList() }.collectAsState(emptyList()))

@Composable
inline fun <reified T: Item<*>> Flow<List<CacheStateOld<T>>>.collectAsSingleFlowOld(): State<List<T>> = this.map { it.filterIsInstance<CacheStateOld.Done<T>>().map { it.data } }.distinctUntilChanged().collectAsState(emptyList())

@Composable
fun <T: AliasedItem<*>> Flow<CacheState<T>>.collectAsLoadingState(id: String = "Unbekannt") = this.collectAsState(CacheState.Loading(id))

@Composable
fun <T: AliasedItem<*>> Flow<CacheState<T>>.collectAsResultingFlow() = this.filterIsInstance<CacheState.Done<T>>().map { it.data }.collectAsState(null)

@Composable
inline fun <reified T: AliasedItem<*>> List<Flow<CacheState<T>>>.collectAsResultingFlow(): State<List<T>> = if (this.isEmpty()) produceState(emptyList()) {} else (combine(this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }) { it.toList() }.collectAsState(emptyList()))

@Composable
inline fun <reified T: AliasedItem<*>> Flow<List<CacheState<T>>>.collectAsSingleFlow(): State<List<T>> = this.map { it.filterIsInstance<CacheState.Done<T>>().map { it.data } }.distinctUntilChanged().collectAsState(emptyList())