package plus.vplan.app.domain.model.schulverwalter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.Item

data class Year(
    val id: Int,
    val name: String,
    val from: LocalDate,
    val to: LocalDate,
    val intervalIds: List<Int>,
    val cachedAt: Instant
) : Item {
    override fun getEntityId(): String = this.id.toString()

    val intervals: Flow<List<CacheState<Interval>>> = combine(intervalIds.map { App.intervalSource.getById(it) }) { it.toList() }
}
