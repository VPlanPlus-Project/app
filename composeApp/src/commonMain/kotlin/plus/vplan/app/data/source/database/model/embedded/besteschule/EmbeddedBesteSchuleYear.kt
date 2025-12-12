package plus.vplan.app.data.source.database.model.embedded.besteschule

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleYear
import plus.vplan.app.domain.model.besteschule.BesteSchuleYear

data class EmbeddedBesteSchuleYear(
    @Embedded val besteSchuleYear: DbBesteschuleYear,
    @Relation(
        parentColumn = "id",
        entityColumn = "year_id",
        entity = DbBesteSchuleInterval::class
    ) val intervals: List<DbBesteSchuleInterval>
) {
    fun toModel() = BesteSchuleYear(
        id = this.besteSchuleYear.id,
        from = this.besteSchuleYear.from,
        to = this.besteSchuleYear.to,
        name = this.besteSchuleYear.name,
        cachedAt = this.besteSchuleYear.cachedAt,
        intervalIds = this.intervals.map { it.id }.toSet()
    )
}