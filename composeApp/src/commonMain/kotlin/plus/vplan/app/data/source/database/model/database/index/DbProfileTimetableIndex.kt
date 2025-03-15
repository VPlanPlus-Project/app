package plus.vplan.app.data.source.database.model.database.index

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import kotlin.uuid.Uuid

@Entity(
    tableName = "profile_timetable_index",
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
data class DbProfileTimetableIndex(
    @ColumnInfo("profile_id") val profileId: Uuid,
    @ColumnInfo("timetable_lesson_id") val timetableLessonId: Uuid
)
