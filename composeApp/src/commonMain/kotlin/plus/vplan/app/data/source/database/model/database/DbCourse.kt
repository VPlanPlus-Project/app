package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.Instant

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
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "indiware_id") val indiwareId: String?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Int?,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
)