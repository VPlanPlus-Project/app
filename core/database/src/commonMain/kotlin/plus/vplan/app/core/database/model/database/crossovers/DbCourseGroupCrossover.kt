package plus.vplan.app.core.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.core.database.model.database.DbCourse
import plus.vplan.app.core.database.model.database.DbGroup
import kotlin.uuid.Uuid

@Entity(
    tableName = "course_group_crossover",
    primaryKeys = ["course_id", "group_id"],
    indices = [
        Index("course_id"),
        Index("group_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbCourse::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
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
    ]
)
data class DbCourseGroupCrossover(
    @ColumnInfo(name = "course_id") val courseId: Uuid,
    @ColumnInfo(name = "group_id") val groupId: Uuid
)
