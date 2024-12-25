package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbHoliday
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.domain.model.Holiday

data class EmbeddedHoliday(
    @Embedded val holiday: DbHoliday,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool
) {
    fun toModel(): Holiday {
        return Holiday(
            id = holiday.id,
            date = holiday.date,
            school = school.toModel()
        )
    }
}
