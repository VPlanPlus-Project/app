package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "timetable_week_limitation",
    primaryKeys = ["timetable_lesson_id", "week_id"],
    foreignKeys = [
        ForeignKey(
            entity = DbTimetableLesson::class,
            parentColumns = ["id"],
            childColumns = ["timetable_lesson_id"],
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
    ],
    indices = [
        Index(value = ["timetable_lesson_id"], unique = false),
        Index(value = ["week_id"], unique = false)
    ]
)
data class DbTimetableWeekLimitation(
    @ColumnInfo(name = "timetable_lesson_id") val timetableLessonId: Uuid,
    @ColumnInfo(name = "week_id") val weekId: String
)