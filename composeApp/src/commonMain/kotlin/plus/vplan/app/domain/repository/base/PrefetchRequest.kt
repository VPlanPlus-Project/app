package plus.vplan.app.domain.repository.base

/**
 * Sealed class representing include/prefetch instructions for entity relationships.
 * Used to build type-safe prefetch requests that eliminate N+1 database queries.
 */
sealed class PrefetchInstruction {
    data object Include : PrefetchInstruction()
    data class Nested(val children: Map<String, PrefetchInstruction> = emptyMap()) : PrefetchInstruction()
    data object Exclude : PrefetchInstruction()
}

/**
 * DSL marker for prefetch request builders
 */
@DslMarker
annotation class PrefetchDsl

/**
 * Base interface for all prefetch request builders.
 * Implement this for each entity type that supports prefetching relationships.
 */
@PrefetchDsl
interface PrefetchRequestBuilder<T> {
    val includedRelations: MutableMap<String, PrefetchInstruction>
    
    /**
     * Include a relationship without any nested prefetching
     */
    fun include(name: String) {
        includedRelations[name] = PrefetchInstruction.Include
    }
    
    /**
     * Include a relationship with nested prefetching
     */
    fun nested(name: String, builder: PrefetchRequestBuilder<*>.() -> Unit) {
        val nestedBuilder = GenericPrefetchRequestBuilder()
        nestedBuilder.builder()
        includedRelations[name] = PrefetchInstruction.Nested(nestedBuilder.build())
    }
    
    fun build(): Map<String, PrefetchInstruction> = includedRelations.toMap()
}

/**
 * Generic builder for nested prefetch requests
 */
@PrefetchDsl
class GenericPrefetchRequestBuilder : PrefetchRequestBuilder<Any> {
    override val includedRelations = mutableMapOf<String, PrefetchInstruction>()
}

/**
 * Creates a prefetch request for an entity type
 */
fun prefetchRequest(builder: PrefetchRequestBuilder<*>.() -> Unit): Map<String, PrefetchInstruction> {
    val requestBuilder = GenericPrefetchRequestBuilder()
    requestBuilder.builder()
    return requestBuilder.build()
}
