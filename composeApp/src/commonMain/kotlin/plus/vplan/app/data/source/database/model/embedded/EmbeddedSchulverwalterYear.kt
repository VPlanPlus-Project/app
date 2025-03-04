package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterYear
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.domain.model.schulverwalter.Year

data class EmbeddedSchulverwalterYear(
    @Embedded val year: DbSchulverwalterYear,
    @Relation(
        parentColumn = "id",
        entityColumn = "year_id",
        entity = FKSchulverwalterYearSchulverwalterInterval::class
    ) val intervals: List<FKSchulverwalterYearSchulverwalterInterval>
) {
    fun toModel() = Year(
        id = year.id,
        name = year.name,
        from = year.from,
        to = year.to,
        intervalIds = intervals.map { it.intervalId },
        cachedAt = year.cachedAt
    )
}