package plus.vplan.app.core.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbSubstitutionPlanLesson
import kotlin.uuid.Uuid

@Entity(
    tableName = "substitution_plan_group_crossover",
    primaryKeys = ["group_id", "substitution_plan_lesson_id"],
    indices = [
        Index(value = ["group_id"], unique = false),
        Index(value = ["substitution_plan_lesson_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["group_id"],
            entity = DbGroup::class,
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
data class DbSubstitutionPlanGroupCrossover(
    @ColumnInfo(name = "group_id") val groupId: Uuid,
    @ColumnInfo(name = "substitution_plan_lesson_id") val substitutionPlanLessonId: Uuid
)