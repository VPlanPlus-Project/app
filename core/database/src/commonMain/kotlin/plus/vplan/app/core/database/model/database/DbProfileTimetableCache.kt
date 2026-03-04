package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "profile_timetable_cache",
    primaryKeys = ["profile_id", "timetable_lesson_id"],
    foreignKeys = [
        ForeignKey(
            entity = DbProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbTimetableLesson::class,
            parentColumns = ["id"],
            childColumns = ["timetable_lesson_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profile_id"),
        Index("timetable_lesson_id")
    ]
)
data class DbProfileTimetableCache(
    @ColumnInfo("profile_id") val profileId: Uuid,
    @ColumnInfo("timetable_lesson_id") val timetableLessonId: Uuid
)
