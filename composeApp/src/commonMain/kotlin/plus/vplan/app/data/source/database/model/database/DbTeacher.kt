package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.Teacher

@Entity(
    tableName = "school_teachers",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["school_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbTeacher(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "school_id") val schoolId: Int,
    @ColumnInfo(name = "name") val name: String
) {
    fun toModel(): Teacher {
        return Teacher(
            id = id,
            school = schoolId,
            name = name
        )
    }
}