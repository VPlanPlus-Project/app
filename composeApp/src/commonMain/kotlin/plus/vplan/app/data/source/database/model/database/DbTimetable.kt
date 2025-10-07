package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.Timetable
import plus.vplan.app.domain.repository.Stundenplan24Repository
import kotlin.uuid.Uuid

@Entity(
    tableName = "timetables",
    primaryKeys = ["school_id", "week_id"],
    indices = [
        Index(value = ["school_id"], unique = false),
        Index(value = ["week_id"], unique = false),
        Index(value = ["id"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbWeek::class,
            parentColumns = ["id"],
            childColumns = ["week_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbTimetable(
    @ColumnInfo(name = "id") val id: Uuid,
    @ColumnInfo(name = "school_id") val schoolId: Uuid,
    @ColumnInfo(name = "week_id") val weekId: String,
    @ColumnInfo(name = "data_state") val dataState: Stundenplan24Repository.HasData
) {
    fun toModel() = Timetable(
        id = id,
        schoolId = schoolId,
        weekId = weekId,
        dataState = dataState
    )
}