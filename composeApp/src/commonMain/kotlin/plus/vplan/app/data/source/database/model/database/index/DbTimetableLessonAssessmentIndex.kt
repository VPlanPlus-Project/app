package plus.vplan.app.data.source.database.model.database.index

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import kotlin.uuid.Uuid

@Entity(
    tableName = "timetable_lesson_assessment_index",
    primaryKeys = ["timetable_lesson_id", "assessment_id"],
    indices = [
        Index("timetable_lesson_id"),
        Index("assessment_id"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTimetableLesson::class,
            parentColumns = ["id"],
            childColumns = ["timetable_lesson_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbAssessment::class,
            parentColumns = ["id"],
            childColumns = ["assessment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbTimetableLessonAssessmentIndex(
    @ColumnInfo("timetable_lesson_id") val timetableLessonId: Uuid,
    @ColumnInfo("assessment_id") val assessmentId: Int
)