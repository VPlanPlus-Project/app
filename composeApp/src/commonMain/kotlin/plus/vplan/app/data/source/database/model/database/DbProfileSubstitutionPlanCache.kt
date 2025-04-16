@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "profile_substitution_plan_cache",
    primaryKeys = ["profile_id", "substitution_lesson_id"],
    foreignKeys = [
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbSubstitutionPlanLesson::class,
            parentColumns = ["id"],
            childColumns = ["substitution_lesson_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profile_id"),
        Index("substitution_lesson_id")
    ]
)
data class DbProfileSubstitutionPlanCache(
    @ColumnInfo("profile_id") val profileId: Uuid,
    @ColumnInfo("substitution_lesson_id") val substitutionPlanLessonId: Uuid
)
