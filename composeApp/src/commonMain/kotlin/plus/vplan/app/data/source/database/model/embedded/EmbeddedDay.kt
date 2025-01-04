package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Day

data class EmbeddedDay(
    @Embedded val day: DbDay,
    @Relation(
        parentColumn = "week_id",
        entityColumn = "id",
        entity = DbWeek::class
    ) val week: EmbeddedWeek,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool
) {
    fun toModel(): Day {
        return Day(
            id = day.id,
            date = day.date,
            info = day.info,
            week = Cacheable.Loaded(week.toModel()),
            school = Cacheable.Loaded(school.toModel())
        )
    }
}
