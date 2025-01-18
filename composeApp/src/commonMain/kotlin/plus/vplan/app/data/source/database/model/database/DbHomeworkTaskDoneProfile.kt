package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "homework_task_done_profile",
    primaryKeys = ["task_id", "profile_id"],
    indices = [
        Index("task_id"),
        Index("profile_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbHomeworkTask::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbHomeworkTaskDoneProfile(
    @ColumnInfo(name = "task_id") val taskId: Int,
    @ColumnInfo(name = "profile_id") val profileId: Uuid
)
