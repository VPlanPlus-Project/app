package plus.vplan.app.data.source.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbGroup

@Entity(
    tableName = "default_lesson_group_crossover",
    primaryKeys = ["default_lesson_id", "group_id"],
    foreignKeys = [
        ForeignKey(
            entity = DbDefaultLesson::class,
            parentColumns = ["id"],
            childColumns = ["default_lesson_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("default_lesson_id"),
        Index("group_id")
    ]
)
data class DbDefaultLessonGroupCrossover(
    @ColumnInfo(name = "default_lesson_id") val defaultLessonId: Int,
    @ColumnInfo(name = "group_id") val groupId: Int
)
