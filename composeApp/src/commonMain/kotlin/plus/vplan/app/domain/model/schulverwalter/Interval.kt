package plus.vplan.app.domain.model.schulverwalter

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item

data class Interval(
    val id: Int,
    val name: String,
    val type: Type,
    val from: LocalDate,
    val to: LocalDate,
    val includedIntervalId: Int?,
    val yearId: Int,
    val collectionIds: List<Int>,
    val cachedAt: Instant
): Item<DataTag> {
    override fun getEntityId(): String = this.id.toString()
    enum class Type {
        SEK1, SEK2;

        companion object {
            fun fromString(type: String): Type {
                return when (type.lowercase().replace(" ", "")) {
                    "sek1", "seki" -> SEK1
                    "sek2", "sekii", "jg11", "jg12", "jg13" -> SEK2
                    else -> throw IllegalArgumentException("Unknown type: $type")
                }
            }
        }
    }

    val year: Flow<CacheState<Year>> = App.yearSource.getById(yearId)
    val includedInterval: Flow<CacheState<Interval>>? = includedIntervalId?.let { App.intervalSource.getById(it) }
}