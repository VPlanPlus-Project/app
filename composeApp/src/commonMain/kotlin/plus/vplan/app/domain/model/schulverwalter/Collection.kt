package plus.vplan.app.domain.model.schulverwalter

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item

data class Collection(
    val id: Int,
    val type: String,
    val name: String,
    val subjectId: Int,
    val intervalId: Int,
    val gradeIds: List<Int>,
    val givenAt: LocalDate,
    val cachedAt: Instant
): Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    val interval by lazy { App.intervalSource.getById(intervalId) }
    val subject by lazy { App.subjectSource.getById(subjectId) }

    val grades by lazy { if (this.gradeIds.isEmpty()) flowOf(emptyList()) else combine(this.gradeIds.map { App.gradeSource.getById(it).filterIsInstance<CacheStateOld.Done<Grade>>().map { gradeState -> gradeState.data } }) { it.toList() } }
}
