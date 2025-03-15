package plus.vplan.app.data.source.database.model.database.index

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import kotlin.uuid.Uuid

@Entity(
    tableName = "timetable_lesson_homework_index",
    primaryKeys = ["timetable_lesson_id", "homework_id"],
    indices = [
        Index("timetable_lesson_id"),
        Index("homework_id"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTimetableLesson::class,
            parentColumns = ["id"],
            childColumns = ["timetable_lesson_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbHomework::class,
            parentColumns = ["id"],
            childColumns = ["homework_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbTimetableLessonHomeworkIndex(
    @ColumnInfo("timetable_lesson_id") val timetableLessonId: Uuid,
    @ColumnInfo("homework_id") val homeworkId: Int
)