package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Entity(
    tableName = "homework",
    primaryKeys = ["id"],
    indices = [
        Index("id", unique = true),
        Index("subject_instance_id"),
        Index("group_id"),
        Index("created_by_vpp_id"),
        Index("created_by_profile_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["created_by_profile_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbHomework(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "subject_instance_id") val subjectInstanceId: Uuid?,
    @ColumnInfo(name = "group_id") val groupId: Uuid?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "due_to") val dueTo: LocalDate,
    @ColumnInfo(name = "created_by_vpp_id") val createdBy: Int?,
    @ColumnInfo(name = "created_by_profile_id") val createdByProfileId: Uuid?,
    @ColumnInfo(name = "is_public") val isPublic: Boolean,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)
