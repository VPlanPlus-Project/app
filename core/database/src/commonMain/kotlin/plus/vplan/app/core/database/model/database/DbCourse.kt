@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "courses",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["teacher_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTeacher::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ]
)
data class DbCourse(
    @ColumnInfo(name = "id") val id: Uuid,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Uuid?,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)