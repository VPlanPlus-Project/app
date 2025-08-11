package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalTime
import plus.vplan.app.domain.model.LessonTime
import kotlin.uuid.Uuid

@Entity(
    tableName = "lesson_times",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["group_id"], unique = false)
  ],
    foreignKeys = [
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbLessonTime(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "start_time") val startTime: LocalTime,
    @ColumnInfo(name = "end_time") val endTime: LocalTime,
    @ColumnInfo(name = "lesson_number") val lessonNumber: Int,
    @ColumnInfo(name = "group_id") val groupId: Uuid,
    @ColumnInfo(name = "interpolated") val interpolated: Boolean
) {
    fun toModel() = LessonTime(
        id = id,
        start = startTime,
        end = endTime,
        lessonNumber = lessonNumber,
        group = groupId,
        interpolated = interpolated
    )
}