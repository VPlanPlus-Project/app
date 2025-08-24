package plus.vplan.app.domain.model.schulverwalter

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.captureError
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item

data class Interval(
    override val id: Int,
    val name: String,
    val type: Type,
    val from: LocalDate,
    val to: LocalDate,
    val includedIntervalId: Int?,
    val yearId: Int,
    val collectionIds: List<Int>,
    val cachedAt: Instant
): Item<Int, DataTag> {
    override val tags: Set<DataTag> = emptySet()

    sealed class Type {
        data object Sek1: Type()
        data object Sek2: Type()
        data class Other(val raw: String): Type()

        companion object {
            fun fromString(type: String): Type {
                return when (type.lowercase().replace(" ", "")) {
                    "sek1", "seki" -> Sek1
                    "sek2", "sekii", "jg11", "jg12", "jg13" -> Sek2
                    else -> {
                        captureError("Interval", "Unknown type: $type")
                        Other(type)
                    }
                }
            }
        }
    }

    val year: Flow<CacheState<Year>> = App.yearSource.getById(yearId)
    val includedInterval: Flow<CacheState<Interval>>? = includedIntervalId?.let { App.intervalSource.getById(it) }
}