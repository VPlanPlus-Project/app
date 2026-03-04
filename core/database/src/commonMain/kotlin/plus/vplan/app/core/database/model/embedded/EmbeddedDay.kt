package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.core.database.model.database.DbDay
import plus.vplan.app.core.database.model.database.DbSchool
import plus.vplan.app.core.database.model.database.DbWeek
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.School

data class EmbeddedDay(
    @Embedded val day: DbDay,
    @Relation(
        parentColumn = "week_id",
        entityColumn = "id",
        entity = DbWeek::class
    ) val week: DbWeek?,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool
) {
    fun toModel() = Day(
        id = day.id,
        date = day.date,
        info = day.info,
        week = week?.toModel(),
        school = school.toModel() as School.AppSchool,
        dayType = if (day.date.dayOfWeek.isoDayNumber > school.sp24SchoolDetails!!.daysPerWeek) Day.DayType.WEEKEND else Day.DayType.REGULAR,
        nextSchoolDay = null,
    )
}