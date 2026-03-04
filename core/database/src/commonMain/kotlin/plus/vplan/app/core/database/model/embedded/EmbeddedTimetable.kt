package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbTimetable
import plus.vplan.app.core.database.model.database.DbWeek
import plus.vplan.app.core.model.Timetable

data class EmbeddedTimetable(
    @Embedded val timetable: DbTimetable,
    @Relation(
        parentColumn = "week_id",
        entityColumn = "id",
        entity = DbWeek::class,
    ) val week: DbWeek,
) {
    fun toModel() = Timetable(
        id = timetable.id,
        schoolId = timetable.schoolId,
        week = week.toModel(),
        dataState = timetable.dataState,
    )
}