package plus.vplan.app.data.source.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson

@Entity(
    tableName = "substitution_plan_room_crossover",
    primaryKeys = ["room_id", "substitution_plan_lesson_id"],
    indices = [
        Index(value = ["room_id"], unique = false),
        Index(value = ["substitution_plan_lesson_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["room_id"],
            entity = DbRoom::class,
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
data class DbSubstitutionPlanRoomCrossover(
    @ColumnInfo(name = "room_id") val roomId: Int,
    @ColumnInfo(name = "substitution_plan_lesson_id") val substitutionPlanLessonId: String
)