package plus.vplan.app.domain.repository.besteschule

import plus.vplan.app.domain.model.besteschule.BesteSchuleCollection
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.repository.base.PrefetchDsl
import plus.vplan.app.domain.repository.base.PrefetchInstruction
import plus.vplan.app.domain.repository.base.PrefetchRequestBuilder

/**
 * Type-safe prefetch request builders for BesteSchule entities.
 * 
 * Example usage:
 * ```kotlin
 * gradesRepository.getGrades(
 *     includes = GradePrefetchRequest {
 *         collection {
 *             interval { year() }
 *             teacher()
 *         }
 *     }
 * )
 * ```
 */

@PrefetchDsl
class GradePrefetchRequestBuilder : PrefetchRequestBuilder<BesteSchuleGrade> {
    override val includedRelations = mutableMapOf<String, PrefetchInstruction>()
    
    /**
     * Include the collection with optional nested prefetching
     */
    fun collection(builder: CollectionPrefetchRequestBuilder.() -> Unit) {
        val nestedBuilder = CollectionPrefetchRequestBuilder()
        nestedBuilder.builder()
        includedRelations["collection"] = PrefetchInstruction.Nested(nestedBuilder.build())
    }
}

@PrefetchDsl
class CollectionPrefetchRequestBuilder : PrefetchRequestBuilder<BesteSchuleCollection> {
    override val includedRelations = mutableMapOf<String, PrefetchInstruction>()
    
    /**
     * Include the interval with optional nested prefetching
     */
    fun interval(builder: IntervalPrefetchRequestBuilder.() -> Unit) {
        val nestedBuilder = IntervalPrefetchRequestBuilder()
        nestedBuilder.builder()
        includedRelations["interval"] = PrefetchInstruction.Nested(nestedBuilder.build())
    }
    
    /**
     * Include the teacher
     */
    fun teacher() {
        includedRelations["teacher"] = PrefetchInstruction.Include
    }
    
    /**
     * Include the subject
     */
    fun subject() {
        includedRelations["subject"] = PrefetchInstruction.Include
    }
}

@PrefetchDsl
class IntervalPrefetchRequestBuilder : PrefetchRequestBuilder<BesteSchuleInterval> {
    override val includedRelations = mutableMapOf<String, PrefetchInstruction>()
    
    /**
     * Include the year
     */
    fun year() {
        includedRelations["year"] = PrefetchInstruction.Include
    }
    
    /**
     * Include the included interval (parent interval)
     */
    fun includedInterval() {
        includedRelations["includedInterval"] = PrefetchInstruction.Include
    }
}

/**
 * Create a prefetch request for grades
 */
inline fun GradePrefetchRequest(builder: GradePrefetchRequestBuilder.() -> Unit): Map<String, PrefetchInstruction> {
    val requestBuilder = GradePrefetchRequestBuilder()
    requestBuilder.builder()
    return requestBuilder.build()
}

/**
 * Create a prefetch request for collections
 */
inline fun CollectionPrefetchRequest(builder: CollectionPrefetchRequestBuilder.() -> Unit): Map<String, PrefetchInstruction> {
    val requestBuilder = CollectionPrefetchRequestBuilder()
    requestBuilder.builder()
    return requestBuilder.build()
}

/**
 * Create a prefetch request for intervals
 */
inline fun IntervalPrefetchRequest(builder: IntervalPrefetchRequestBuilder.() -> Unit): Map<String, PrefetchInstruction> {
    val requestBuilder = IntervalPrefetchRequestBuilder()
    requestBuilder.builder()
    return requestBuilder.build()
}
