@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "subject_instance",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["teacher_id"], unique = false)
    ]
)
data class DbSubjectInstance(
    @ColumnInfo(name = "id") val id: Uuid,
    @ColumnInfo(name = "subject") val subject: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Uuid?,
    @ColumnInfo(name = "course_id") val courseId: Uuid?,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)