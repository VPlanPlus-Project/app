package plus.vplan.app.data.source.database.model.database.index

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import kotlin.uuid.Uuid

@Entity(
    tableName = "substitution_plan_lesson_assessment_index",
    primaryKeys = ["substitution_plan_lesson_id", "assessment_id"],
    indices = [
        Index("substitution_plan_lesson_id"),
        Index("assessment_id"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSubstitutionPlanLesson::class,
            parentColumns = ["id"],
            childColumns = ["substitution_plan_lesson_id"],
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
data class DbSubstitutionPlanLessonAssessmentIndex(
    @ColumnInfo("substitution_plan_lesson_id") val substitutionPlanLessonId: Uuid,
    @ColumnInfo("assessment_id") val assessmentId: Int
)