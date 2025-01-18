package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "homework_task_done_account",
    primaryKeys = ["task_id", "vpp_id"],
    indices = [
        Index("task_id"),
        Index("vpp_id")
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
            entity = DbVppId::class,
            parentColumns = ["id"],
            childColumns = ["vpp_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbHomeworkTaskDoneAccount(
    @ColumnInfo(name = "task_id") val taskId: Int,
    @ColumnInfo(name = "vpp_id") val vppId: Int,
    @ColumnInfo(name = "is_done") val isDone: Boolean
)
