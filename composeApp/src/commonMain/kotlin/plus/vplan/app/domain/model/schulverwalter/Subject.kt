package plus.vplan.app.domain.model.schulverwalter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Instant
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.Item

data class Subject(
    val id: Int,
    val name: String,
    val localId: String,
    val collectionIds: List<Int>,
    val cachedAt: Instant
): Item {
    override fun getEntityId(): String = this.id.toString()

    val collections: Flow<List<CacheState<Collection>>> = combine(collectionIds.map { App.collectionSource.getById(it) }) { it.toList() }
}
