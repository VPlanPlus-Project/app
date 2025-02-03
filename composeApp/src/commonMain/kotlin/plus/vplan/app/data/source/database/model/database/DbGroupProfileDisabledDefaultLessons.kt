package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "profiles_group_disabled_default_lessons",
    primaryKeys = ["profile_id", "default_lesson_id"],
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["default_lesson_id"])
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
            entity = DbDefaultLesson::class,
            parentColumns = ["id"],
            childColumns = ["default_lesson_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbGroupProfileDisabledDefaultLessons(
    @ColumnInfo(name = "profile_id") val profileId: Uuid,
    @ColumnInfo(name = "default_lesson_id") val defaultLessonId: Int,
)
