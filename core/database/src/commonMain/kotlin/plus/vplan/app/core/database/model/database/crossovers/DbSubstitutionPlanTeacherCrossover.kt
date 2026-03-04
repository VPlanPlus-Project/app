package plus.vplan.app.core.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.core.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.core.database.model.database.DbTeacher
import kotlin.uuid.Uuid

@Entity(
    tableName = "substitution_teacher_room_crossover",
    primaryKeys = ["teacher_id", "substitution_plan_lesson_id"],
    indices = [
        Index(value = ["teacher_id"], unique = false),
        Index(value = ["substitution_plan_lesson_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            entity = DbTeacher::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["substitution_plan_lesson_id"],
            entity = DbSubstitutionPlanLesson::class,
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbSubstitutionPlanTeacherCrossover(
    @ColumnInfo(name = "teacher_id") val teacherId: Uuid,
    @ColumnInfo(name = "substitution_plan_lesson_id") val substitutionPlanLessonId: Uuid
)