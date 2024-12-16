package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.EntityIdentifier
import kotlin.uuid.Uuid

@Entity(
    tableName = "courses",
    primaryKeys = ["entity_id"],
    indices = [
        Index(value = ["entity_id"], unique = true),
        Index(value = ["teacher_id"], unique = false),
        Index(value = ["group_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbTeacher::class,
            parentColumns = ["entity_id"],
            childColumns = ["teacher_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["entity_id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbCourse(
    @ColumnInfo(name = "entity_id") val id: Uuid = Uuid.random(),
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "teacher_id") val teacherId: Uuid,
    @ColumnInfo(name = "group_id") val groupId: Uuid
)

@Entity(
    tableName = "course_identifiers",
    primaryKeys = ["course_id", "origin", "value"],
    indices = [Index(value = ["course_id", "origin", "value"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = DbCourse::class,
            parentColumns = ["entity_id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbCourseIdentifier(
    @ColumnInfo(name = "course_id") val courseId: Uuid,
    @ColumnInfo(name = "origin") val origin: EntityIdentifier.Origin,
    @ColumnInfo(name = "value") val value: String
) {
    fun toModel(): EntityIdentifier {
        return EntityIdentifier(
            entityId = courseId,
            origin = origin,
            value = value
        )
    }
}