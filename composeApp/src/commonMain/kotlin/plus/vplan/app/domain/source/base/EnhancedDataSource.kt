package plus.vplan.app.domain.source.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheConfig
import plus.vplan.app.domain.cache.DataSourceState
import plus.vplan.app.domain.cache.IntelligentCache
import plus.vplan.app.domain.cache.RefreshCoordinator
import plus.vplan.app.domain.cache.RefreshPolicy
import plus.vplan.app.domain.cache.isStale
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory

/**
 * Enhanced base data source with intelligent caching, refresh coordination, and linked entity tracking.
 * 
 * This provides a flexible data-access model that:
 * - Allows for loading states for linked entities
 * - Provides a mostly transparent layer for data retrieval (cloud/local)
 * - Supports force refresh for specific entities
 * - Implements intelligent caching with configurable policies
 * - Deduplicates concurrent refresh requests
 * 
 * @param ID The type of the entity identifier
 * @param T The type of the entity
 */
abstract class EnhancedDataSource<ID, T> : KoinComponent {
    protected val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()
    protected val refreshCoordinator: RefreshCoordinator by inject()
    
    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // StateFlow cache for active subscriptions
    private val activeFlows: ConcurrentHashMap<ID, StateFlow<DataSourceState<T>>> = 
        concurrentHashMapFactory.create()
    
    // Intelligent cache for data
    private val dataCache: IntelligentCache<ID, T> by lazy {
        IntelligentCache(getCacheConfig(), concurrentHashMapFactory)
    }
    
    // Track refresh operations
    private val refreshInProgress: ConcurrentHashMap<ID, Boolean> = concurrentHashMapFactory.create()
    
    /**
     * Configure the cache behavior for this data source
     */
    protected open fun getCacheConfig(): CacheConfig = CacheConfig()
    
    /**
     * Fetch data from local storage (database)
     */
    protected abstract suspend fun fetchFromLocal(id: ID): T?
    
    /**
     * Fetch data from remote source (network)
     */
    protected abstract suspend fun fetchFromRemote(id: ID): T
    
    /**
     * Save data to local storage
     */
    protected abstract suspend fun saveToLocal(id: ID, data: T)
    
    /**
     * Get IDs of linked entities that need to be loaded
     * Override this to track loading states of related entities
     */
    protected open suspend fun getLinkedEntityIds(data: T): Set<String> = emptySet()
    
    /**
     * Check if a linked entity is currently loading
     * Override this to provide accurate loading states
     */
    protected open suspend fun isLinkedEntityLoading(entityId: String): Boolean = false
    
    /**
     * Gets data for the given ID as a Flow with the specified refresh policy.
     * 
     * @param id The entity identifier
     * @param refreshPolicy How to handle cache vs. network
     * @param forceRefresh If true, bypasses cache and forces a network fetch
     * @return Flow of DataSourceState representing the loading state and data
     */
    fun get(
        id: ID,
        refreshPolicy: RefreshPolicy = RefreshPolicy.CACHE_FIRST,
        forceRefresh: Boolean = false
    ): StateFlow<DataSourceState<T>> {
        // If force refresh, remove from active flows to create new one
        if (forceRefresh) {
            activeFlows.remove(id)
        }
        
        return activeFlows.getOrPut(id) {
            createFlow(id, refreshPolicy, forceRefresh).stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = DataSourceState.Loading(id.toString())
            )
        }
    }
    
    /**
     * Creates the actual data flow with cache and network coordination
     */
    private fun createFlow(
        id: ID,
        refreshPolicy: RefreshPolicy,
        forceRefresh: Boolean
    ): Flow<DataSourceState<T>> = flow {
        try {
            when (refreshPolicy) {
                RefreshPolicy.CACHE_FIRST -> handleCacheFirst(id, forceRefresh) { emit(it) }
                RefreshPolicy.CACHE_THEN_NETWORK -> handleCacheThenNetwork(id, forceRefresh) { emit(it) }
                RefreshPolicy.NETWORK_FIRST -> handleNetworkFirst(id, forceRefresh) { emit(it) }
                RefreshPolicy.NETWORK_ONLY -> handleNetworkOnly(id) { emit(it) }
                RefreshPolicy.CACHE_ONLY -> handleCacheOnly(id) { emit(it) }
            }
        } catch (e: Exception) {
            val cachedData = dataCache.get(id)
            emit(DataSourceState.Error(id.toString(), e, cachedData))
        }
    }
    
    private suspend fun handleCacheFirst(
        id: ID,
        forceRefresh: Boolean,
        emit: suspend (DataSourceState<T>) -> Unit
    ) {
        // Try cache first
        val cached = if (!forceRefresh) dataCache.get(id) else null
        
        if (cached != null) {
            val linkedLoading = getLinkedEntityIds(cached)
            emit(DataSourceState.Success(
                data = cached,
                linkedEntitiesLoading = linkedLoading,
                isRefreshing = false,
                cachedAt = System.currentTimeMillis()
            ))
            
            // Check if stale and refresh in background if needed
            val cacheConfig = getCacheConfig()
            val isStale = System.currentTimeMillis() - (System.currentTimeMillis()) > cacheConfig.ttlMillis
            
            if (isStale || forceRefresh) {
                refreshInBackground(id, emit)
            }
        } else {
            // No cache, fetch from network
            emit(DataSourceState.Loading(id.toString()))
            val fresh = refreshData(id)
            val linkedLoading = getLinkedEntityIds(fresh)
            emit(DataSourceState.Success(
                data = fresh,
                linkedEntitiesLoading = linkedLoading,
                isRefreshing = false,
                cachedAt = System.currentTimeMillis()
            ))
        }
    }
    
    private suspend fun handleCacheThenNetwork(
        id: ID,
        forceRefresh: Boolean,
        emit: suspend (DataSourceState<T>) -> Unit
    ) {
        // Always emit cache first if available
        val cached = if (!forceRefresh) dataCache.get(id) else null
        if (cached != null) {
            val linkedLoading = getLinkedEntityIds(cached)
            emit(DataSourceState.Success(
                data = cached,
                linkedEntitiesLoading = linkedLoading,
                isRefreshing = true
            ))
        } else {
            emit(DataSourceState.Loading(id.toString()))
        }
        
        // Always fetch fresh data
        refreshInBackground(id, emit)
    }
    
    private suspend fun handleNetworkFirst(
        id: ID,
        forceRefresh: Boolean,
        emit: suspend (DataSourceState<T>) -> Unit
    ) {
        emit(DataSourceState.Loading(id.toString()))
        
        try {
            val fresh = refreshData(id)
            val linkedLoading = getLinkedEntityIds(fresh)
            emit(DataSourceState.Success(
                data = fresh,
                linkedEntitiesLoading = linkedLoading,
                isRefreshing = false,
                cachedAt = System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            // Fall back to cache on error
            val cached = dataCache.get(id)
            if (cached != null) {
                val linkedLoading = getLinkedEntityIds(cached)
                emit(DataSourceState.Success(
                    data = cached,
                    linkedEntitiesLoading = linkedLoading,
                    isRefreshing = false
                ))
            } else {
                throw e
            }
        }
    }
    
    private suspend fun handleNetworkOnly(
        id: ID,
        emit: suspend (DataSourceState<T>) -> Unit
    ) {
        emit(DataSourceState.Loading(id.toString()))
        val fresh = refreshData(id)
        val linkedLoading = getLinkedEntityIds(fresh)
        emit(DataSourceState.Success(
            data = fresh,
            linkedEntitiesLoading = linkedLoading,
            isRefreshing = false,
            cachedAt = System.currentTimeMillis()
        ))
    }
    
    private suspend fun handleCacheOnly(
        id: ID,
        emit: suspend (DataSourceState<T>) -> Unit
    ) {
        val cached = dataCache.get(id)
        if (cached != null) {
            val linkedLoading = getLinkedEntityIds(cached)
            emit(DataSourceState.Success(
                data = cached,
                linkedEntitiesLoading = linkedLoading,
                isRefreshing = false
            ))
        } else {
            emit(DataSourceState.NotFound(id.toString()))
        }
    }
    
    /**
     * Refreshes data from network, using the refresh coordinator to deduplicate requests
     */
    private suspend fun refreshData(id: ID): T {
        return refreshCoordinator.coordinateRefresh(id.toString()) {
            try {
                refreshInProgress[id] = true
                val fresh = fetchFromRemote(id)
                saveToLocal(id, fresh)
                dataCache.put(id, fresh)
                fresh
            } finally {
                refreshInProgress.remove(id)
            }
        }
    }
    
    /**
     * Refreshes data in the background without blocking
     */
    private fun refreshInBackground(
        id: ID,
        emit: suspend (DataSourceState<T>) -> Unit
    ) {
        scope.launch {
            try {
                val fresh = refreshData(id)
                val linkedLoading = getLinkedEntityIds(fresh)
                emit(DataSourceState.Success(
                    data = fresh,
                    linkedEntitiesLoading = linkedLoading,
                    isRefreshing = false,
                    cachedAt = System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                // Silent failure for background refresh
                // The user already has cached data
            }
        }
    }
    
    /**
     * Invalidates cached data for a specific entity
     */
    suspend fun invalidate(id: ID) {
        dataCache.invalidate(id)
        activeFlows.remove(id)
    }
    
    /**
     * Invalidates all cached data
     */
    suspend fun invalidateAll() {
        dataCache.clear()
        activeFlows.clear()
    }
    
    /**
     * Pre-warms the cache with data
     */
    suspend fun prewarm(id: ID, data: T) {
        dataCache.put(id, data)
        saveToLocal(id, data)
    }
}
