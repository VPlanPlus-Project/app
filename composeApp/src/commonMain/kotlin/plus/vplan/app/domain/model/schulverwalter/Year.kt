package plus.vplan.app.domain.model.schulverwalter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item

data class Year(
    val id: Int,
    val name: String,
    val from: LocalDate,
    val to: LocalDate,
    val intervalIds: List<Int>,
    val cachedAt: Instant
) : Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    override val tags: Set<DataTag> = emptySet()

    val intervals: Flow<List<CacheState<Interval>>> = if (intervalIds.isEmpty()) flowOf(emptyList()) else combine(intervalIds.map { App.intervalSource.getById(it) }) { it.toList() }
}
