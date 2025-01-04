package plus.vplan.app.domain.cache

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response

interface CacheableItemSource<T : CachedItem<T>> {
    fun getAll(configuration: FetchConfiguration<T>): Flow<List<Cacheable<T>>>
    fun getById(id: String, configuration: FetchConfiguration<T>): Flow<Cacheable<T>>

    sealed class FetchConfiguration<T> {
        abstract class Fetch<T> : FetchConfiguration<T>()
        class Ignore<T> : FetchConfiguration<T>()
    }
}

interface CachedItem<T> {
    fun getItemId(): String
    fun isConfigSatisfied(
        configuration: CacheableItemSource.FetchConfiguration<T>,
        allowLoading: Boolean
    ): Boolean
}

sealed class Cacheable<T : CachedItem<T>> {
    data class Loaded<T : CachedItem<T>>(val value: T) : Cacheable<T>() {
        override fun getItemId(): String = value.getItemId()
        override fun isConfigSatisfied(configuration: CacheableItemSource.FetchConfiguration<T>, allowLoading: Boolean): Boolean = value.isConfigSatisfied(configuration, allowLoading)
        override fun toValueOrNull(): T = value
    }
    data class Loading<T : CachedItem<T>>(val id: String, val progress: Int) : Cacheable<T>() {
        override fun getItemId(): String = id
        override fun isConfigSatisfied(configuration: CacheableItemSource.FetchConfiguration<T>, allowLoading: Boolean): Boolean = allowLoading
        override fun toValueOrNull(): T? = null
    }
    data class Error<T : CachedItem<T>>(val id: String, val error: Response.Error): Cacheable<T>() {
        override fun getItemId(): String = id
        override fun isConfigSatisfied(configuration: CacheableItemSource.FetchConfiguration<T>, allowLoading: Boolean): Boolean = allowLoading
        override fun toValueOrNull(): T? = null
    }
    data class Uninitialized<T : CachedItem<T>>(val id: String) : Cacheable<T>() {
        override fun getItemId(): String = id
        override fun isConfigSatisfied(configuration: CacheableItemSource.FetchConfiguration<T>, allowLoading: Boolean): Boolean = false
        override fun toValueOrNull(): T? = null
    }
    data class NotExisting<T : CachedItem<T>>(val id: String) : Cacheable<T>() {
        override fun getItemId(): String = id
        override fun isConfigSatisfied(configuration: CacheableItemSource.FetchConfiguration<T>, allowLoading: Boolean): Boolean = allowLoading
        override fun toValueOrNull(): T? = null
    }

    abstract fun getItemId(): String
    abstract fun isConfigSatisfied(configuration: CacheableItemSource.FetchConfiguration<T>, allowLoading: Boolean): Boolean
    abstract fun toValueOrNull(): T?
}