package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Week

@Entity(
    tableName = "weeks",
    primaryKeys = ["school_id", "calendar_week"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["school_id", "calendar_week"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbWeek(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "school_id") val schoolId: Int,
    @ColumnInfo(name = "calendar_week") val calendarWeek: Int,
    @ColumnInfo(name = "start") val start: LocalDate,
    @ColumnInfo(name = "end") val end: LocalDate,
    @ColumnInfo(name = "week_type") val weekType: String,
    @ColumnInfo(name = "week_index") val weekIndex: Int
) {
    fun toModel(): Week {
        return Week(
            id = id,
            calendarWeek = calendarWeek,
            start = start,
            end = end,
            weekType = weekType,
            weekIndex = weekIndex,
            school = schoolId
        )
    }
}