package plus.vplan.app.core.model

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty

sealed class CacheState<out T: Item<*, *>>(val entityId: String) {
    data class Loading(val id: String): CacheState<Nothing>(id)
    data class NotExisting(val id: String): CacheState<Nothing>(id) {
        constructor(): this("Unknown")
    }
    data class Error(val id: String, val error: Response.Error): CacheState<Nothing>(id) {
        constructor(id: Int, error: Response.Error): this(id.toString(), error)
    }
    data class Done<T: Item<*, *>>(val data: T): CacheState<T>(data.id.toString())
}

sealed class AliasState<out T: AliasedItem<*>>(val entityId: String) {
    data class Loading(val id: String): AliasState<Nothing>(id)
    data class NotExisting(val id: String): AliasState<Nothing>(id)
    data class Error(val id: String, val error: Response.Error): AliasState<Nothing>(id) {
        constructor(id: Int, error: Response.Error): this(id.toString(), error)
    }
    data class Done<T: AliasedItem<*>>(val data: T): AliasState<T>(data.id.toHexString())
}

/**
 * Tags that can be used to indicate how much data is already loaded for an entity.
 */
interface DataTag

suspend inline fun <reified T : Item<*, *>> Flow<CacheState<T>>.getFirstValueOld(vararg requiredTags: DataTag): T? {
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

suspend inline fun <reified T : AliasedItem<*>> Flow<AliasState<T>>.getFirstValue(vararg requiredTags: DataTag): T? {
    return this
        .onEach { if (it is AliasState.Error) Logger.e { "Failed to load entity ${it.entityId}: ${it.error}" } }
        .filter { it is AliasState.NotExisting || it is AliasState.Done || it is AliasState.Error }
        .map {
            if (it is AliasState.Done && it.data.tags.all { it in requiredTags }) it.data
            else null
        }
        .onEmpty { throw RuntimeException("Failed to load entity ${T::class.simpleName}, required tags: $requiredTags, Cause: Flow was empty") }
        .catch {
            throw RuntimeException("Failed to load entity ${T::class.simpleName}, required tags: $requiredTags", it)
        }
        .first()
}
