package plus.vplan.app.data.source.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import kotlin.uuid.Uuid

@Entity(
    tableName = "timetable_room_crossover",
    primaryKeys = ["room_id", "timetable_lesson_id"],
    indices = [
        Index(value = ["room_id"], unique = false),
        Index(value = ["timetable_lesson_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTimetableLesson::class,
            parentColumns = ["id"],
            childColumns = ["timetable_lesson_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbRoom::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbTimetableRoomCrossover(
    @ColumnInfo(name = "room_id") val roomId: Int,
    @ColumnInfo(name = "timetable_lesson_id") val timetableLessonId: Uuid,
)
