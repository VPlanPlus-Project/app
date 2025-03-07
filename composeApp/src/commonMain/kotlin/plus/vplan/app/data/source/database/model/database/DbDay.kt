package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Day

@Entity(
    tableName = "day",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["school_id"], unique = false),
        Index(value = ["week_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbWeek::class,
            parentColumns = ["id"],
            childColumns = ["week_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbDay(
    @ColumnInfo("id") val id: String,
    @ColumnInfo("date") val date: LocalDate,
    @ColumnInfo("week_id") val weekId: String,
    @ColumnInfo("school_id") val schoolId: Int,
    @ColumnInfo("info") val info: String?,
) {
    fun toModel(): Day {
        return Day(
            id = id,
            date = date,
            info = info,
            weekId = weekId,
            school = schoolId,
            dayType = Day.DayType.UNKNOWN,
            substitutionPlan = emptyList(),
            timetable = emptyList(),
            assessmentIds = emptyList(),
            homeworkIds = emptyList(),
            nextSchoolDay = null
        )
    }
}