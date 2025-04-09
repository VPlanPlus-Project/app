package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "profile_assessment_index",
    primaryKeys = ["assessment_id", "profile_id"],
    indices = [
        Index("assessment_id"),
        Index("profile_id"),
    ],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            entity = DbProfile::class,
            onDelete = CASCADE
        ),ForeignKey(
            parentColumns = ["id"],
            childColumns = ["assessment_id"],
            entity = DbAssessment::class,
            onDelete = CASCADE
        ),
    ]
)
data class DbProfileAssessmentIndex(
    @ColumnInfo(name = "assessment_id") val assessmentId: Int,
    @ColumnInfo(name = "profile_id") val profileId: Uuid
)