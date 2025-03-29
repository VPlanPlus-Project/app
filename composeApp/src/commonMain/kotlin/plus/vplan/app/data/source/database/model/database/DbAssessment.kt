package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@Entity(
    tableName = "assessments",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = DbSubjectInstance::class,
            parentColumns = ["id"],
            childColumns = ["subject_instance_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["created_by_profile"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subject_instance_id", unique = false),
        Index("created_by_profile", unique = false)
    ]
)
data class DbAssessment(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "created_by") val createdBy: Int?,
    @ColumnInfo(name = "created_by_profile") val createdByProfile: Uuid?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "is_public") val isPublic: Boolean,
    @ColumnInfo(name = "subject_instance_id") val subjectInstanceId: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)