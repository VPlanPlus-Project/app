package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.datetime.DayOfWeek

@Entity(
    tableName = "timetable_lessons",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = DbWeek::class,
            parentColumns = ["id"],
            childColumns = ["week_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbLessonTime::class,
            parentColumns = ["id"],
            childColumns = ["lesson_time_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbTimetableLesson(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: DayOfWeek,
    @ColumnInfo(name = "week_id") val weekId: String,
    @ColumnInfo(name = "lesson_time_id") val lessonTimeId: String,
    @ColumnInfo(name = "subject") val subject: String?,
    @ColumnInfo(name = "version") val version: Int,
)
