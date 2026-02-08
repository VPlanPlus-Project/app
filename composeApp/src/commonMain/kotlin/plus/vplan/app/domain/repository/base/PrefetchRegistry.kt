package plus.vplan.app.domain.repository.base

data class PrefetchError(
    val entityName: String,
    val entityId: Int,
    val error: Throwable
)

data class PrefetchResult(
    val successCount: Int,
    val errors: List<PrefetchError>
)

interface PrefetchHandler {
    suspend fun prefetch(ids: List<Int>, includes: Map<String, PrefetchInstruction>): PrefetchResult
}

object PrefetchRegistry {
    private val handlers = mutableMapOf<String, PrefetchHandler>()

    fun register(entityName: String, handler: PrefetchHandler) {
        handlers[entityName] = handler
    }

    fun unregister(entityName: String) {
        handlers.remove(entityName)
    }

    suspend fun prefetch(entityName: String, ids: List<Int>, includes: Map<String, PrefetchInstruction>): PrefetchResult {
        return handlers[entityName]?.prefetch(ids, includes)
            ?: PrefetchResult(0, listOf(PrefetchError(entityName, 0, IllegalStateException("No handler registered for $entityName"))))
    }

    fun isRegistered(entityName: String): Boolean {
        return handlers.containsKey(entityName)
    }
}

interface PrefetchableRepository {
    val entityName: String
    fun registerForPrefetching()
    fun unregisterFromPrefetching()
}

/**
 * Extension property to safely get nested prefetch instructions
 */
val PrefetchInstruction?.nestedInstructions: Map<String, PrefetchInstruction>
    get() = when (this) {
        is PrefetchInstruction.Nested -> this.children
        else -> emptyMap()
    }
