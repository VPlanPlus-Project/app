package plus.vplan.app.data.source.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbGroup
import kotlin.uuid.Uuid

@Entity(
    tableName = "fk_subject_instance_group",
    primaryKeys = ["subject_instance_id", "group_id"],
    foreignKeys = [
        ForeignKey(
            entity = DbSubjectInstance::class,
            parentColumns = ["id"],
            childColumns = ["subject_instance_id"],
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
        Index("subject_instance_id"),
        Index("group_id")
    ]
)
data class FKSubjectInstanceGroup(
    @ColumnInfo(name = "subject_instance_id") val subjectInstanceId: Int,
    @ColumnInfo(name = "group_id") val groupId: Uuid
)
