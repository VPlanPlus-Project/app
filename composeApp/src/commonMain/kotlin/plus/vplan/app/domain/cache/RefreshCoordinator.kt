package plus.vplan.app.domain.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory

/**
 * Coordinates refresh requests to prevent duplicate concurrent fetches of the same entity.
 * This is crucial for performance when multiple UI components request the same data simultaneously.
 */
class RefreshCoordinator(
    private val concurrentHashMapFactory: ConcurrentHashMapFactory
) {
    // Stores in-flight refresh operations keyed by entity ID
    private val inFlightRequests: ConcurrentHashMap<String, CompletableDeferred<Any?>> = 
        concurrentHashMapFactory.create()
    
    private val mutex = Mutex()
    
    /**
     * Executes a refresh operation, deduplicating concurrent requests for the same entity.
     * 
     * @param entityId Unique identifier for the entity being refreshed
     * @param refresh The refresh operation to execute
     * @return The refreshed data
     */
    suspend fun <T> coordinateRefresh(
        entityId: String,
        refresh: suspend () -> T
    ): T {
        // Check if there's already an in-flight request
        val existingRequest = inFlightRequests[entityId]
        if (existingRequest != null) {
            @Suppress("UNCHECKED_CAST")
            return existingRequest.await() as T
        }
        
        // Create a new deferred result for this request
        val deferred = CompletableDeferred<Any?>()
        
        return mutex.withLock {
            // Double-check pattern: another coroutine might have started the request
            val existingAfterLock = inFlightRequests[entityId]
            if (existingAfterLock != null) {
                @Suppress("UNCHECKED_CAST")
                return@withLock existingAfterLock.await() as T
            }
            
            // Register this request as in-flight
            inFlightRequests[entityId] = deferred
            
            try {
                // Execute the refresh operation
                val result = refresh()
                deferred.complete(result)
                result
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
                throw e
            } finally {
                // Clean up the in-flight request
                inFlightRequests.remove(entityId)
            }
        }
    }
    
    /**
     * Cancels any in-flight refresh operation for the given entity.
     * This is useful when the UI component is no longer interested in the result.
     */
    fun cancelRefresh(entityId: String) {
        inFlightRequests.remove(entityId)?.cancel()
    }
    
    /**
     * Cancels all in-flight refresh operations.
     */
    fun cancelAll() {
        inFlightRequests.clear()
    }
}
