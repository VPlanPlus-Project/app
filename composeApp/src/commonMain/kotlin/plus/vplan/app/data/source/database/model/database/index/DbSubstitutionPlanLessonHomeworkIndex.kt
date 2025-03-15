package plus.vplan.app.data.source.database.model.database.index

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import kotlin.uuid.Uuid

@Entity(
    tableName = "substitution_plan_lesson_homework_index",
    primaryKeys = ["substitution_plan_lesson_id", "homework_id"],
    indices = [
        Index("substitution_plan_lesson_id"),
        Index("homework_id"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSubstitutionPlanLesson::class,
            parentColumns = ["id"],
            childColumns = ["substitution_plan_lesson_id"],
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
data class DbSubstitutionPlanLessonHomeworkIndex(
    @ColumnInfo("substitution_plan_lesson_id") val substitutionPlanLessonId: Uuid,
    @ColumnInfo("homework_id") val homeworkId: Int
)