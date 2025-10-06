package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.DayOfWeek
import kotlin.uuid.Uuid

@Entity(
    tableName = "timetable_lessons",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["lesson_time_id"], unique = false),
        Index(value = ["week_id"], unique = false),
        Index(value = ["timetable_id"], unique = false),
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
            entity = DbLessonTime::class,
            parentColumns = ["id"],
            childColumns = ["lesson_time_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbTimetable::class,
            parentColumns = ["id"],
            childColumns = ["timetable_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ]
)
data class DbTimetableLesson(
    @ColumnInfo(name = "id") val id: Uuid,
    @ColumnInfo(name = "timetable_id") val timetableId: Uuid,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: DayOfWeek,
    @ColumnInfo(name = "week_id") val weekId: String,
    @ColumnInfo(name = "lesson_time_id") val lessonTimeId: String,
    @ColumnInfo(name = "subject") val subject: String?,
    @ColumnInfo(name = "week_type") val weekType: String?,
)
