package plus.vplan.app.data.source.database.model.database.crossovers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson

@Entity(
    tableName = "timetable_group_crossover",
    primaryKeys = ["group_id", "timetable_lesson_id"],
    indices = [
        Index(value = ["group_id"], unique = false),
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
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbTimetableGroupCrossover(
    @ColumnInfo(name = "group_id") val groupId: Int,
    @ColumnInfo(name = "timetable_lesson_id") val timetableLessonId: String,
)
