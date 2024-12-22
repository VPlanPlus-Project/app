package plus.vplan.app.data.source.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson

@Entity(
    tableName = "timetable_teacher_crossover",
    primaryKeys = ["teacher_id", "timetable_lesson_id"],
    indices = [
        Index(value = ["teacher_id"], unique = false),
        Index(value = ["timetable_lesson_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTimetableLesson::class,
            parentColumns = ["id"],
            childColumns = ["timetable_lesson_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbTeacher::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbTimetableTeacher(
    @ColumnInfo(name = "teacher_id") val teacherId: Int,
    @ColumnInfo(name = "timetable_lesson_id") val timetableLessonId: String,
)