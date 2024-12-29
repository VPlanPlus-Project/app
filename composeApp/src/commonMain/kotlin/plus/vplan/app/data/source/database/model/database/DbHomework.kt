package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "homework",
    primaryKeys = ["id"],
    indices = [
        Index("id", unique = true),
        Index("default_lesson_id"),
        Index("group_id"),
        Index("created_by_vpp_id"),
        Index("created_by_profile_id")
    ],
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
        ),
        ForeignKey(
            entity = DbVppId::class,
            parentColumns = ["id"],
            childColumns = ["created_by_vpp_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
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
    @ColumnInfo(name = "default_lesson_id") val defaultLessonId: String?,
    @ColumnInfo(name = "group_id") val groupId: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "due_to") val dueTo: Instant,
    @ColumnInfo(name = "created_by_vpp_id") val createdBy: Int?,
    @ColumnInfo(name = "created_by_profile_id") val createdByProfileId: Uuid?,
    @ColumnInfo(name = "is_public") val isPublic: Boolean,
)