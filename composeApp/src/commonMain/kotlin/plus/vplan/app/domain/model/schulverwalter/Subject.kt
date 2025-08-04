package plus.vplan.app.domain.model.schulverwalter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item

data class Subject(
    val id: Int,
    val name: String,
    val localId: String,
    val collectionIds: List<Int>,
    val finalGradeId: Int?,
    val cachedAt: Instant
): Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    val collections: Flow<List<CacheState<Collection>>> = if (collectionIds.isEmpty()) flowOf(emptyList()) else combine(collectionIds.map { App.collectionSource.getById(it) }) { it.toList() }
    val finalGrade: Flow<FinalGrade>? by lazy { finalGradeId?.let { App.finalGradeSource.getById(finalGradeId).filterIsInstance<CacheState.Done<FinalGrade>>().map { finalGrade -> finalGrade.data } } }
}
