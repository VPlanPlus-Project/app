package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbWeek
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Week

data class EmbeddedWeek(
    @Embedded val week: DbWeek,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool
) {
    fun toModel(): Week {
        return Week(
            id = week.id,
            calendarWeek = week.calendarWeek,
            start = week.start,
            end = week.end,
            weekType = week.weekType,
            weekIndex = week.weekIndex,
            school = Cacheable.Loaded(school.toModel())
        )
    }
}