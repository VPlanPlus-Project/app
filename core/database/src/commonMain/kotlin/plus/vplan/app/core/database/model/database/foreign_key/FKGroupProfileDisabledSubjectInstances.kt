package plus.vplan.app.core.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbSubjectInstance
import kotlin.uuid.Uuid

@Entity(
    tableName = "fk_group_profile_disabled_subject_instances",
    primaryKeys = ["profile_id", "subject_instance_id"],
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["subject_instance_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbSubjectInstance::class,
            parentColumns = ["id"],
            childColumns = ["subject_instance_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class FKGroupProfileDisabledSubjectInstances(
    @ColumnInfo(name = "profile_id") val profileId: Uuid,
    @ColumnInfo(name = "subject_instance_id") val subjectInstanceId: Uuid,
)
