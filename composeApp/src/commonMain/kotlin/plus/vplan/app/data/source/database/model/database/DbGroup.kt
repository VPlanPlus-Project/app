package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.EntityIdentifier
import kotlin.uuid.Uuid

@Entity(
    tableName = "school_groups",
    primaryKeys = ["entity_id"],
    indices = [
        Index(value = ["entity_id"], unique = true),
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
data class DbGroup(
    @ColumnInfo(name = "entity_id") val id: Uuid,
    @ColumnInfo(name = "school_id") val schoolId: Int,
    @ColumnInfo(name = "name") val name: String
)

@Entity(
    tableName = "school_group_identifiers",
    primaryKeys = ["group_id", "origin", "value"],
    indices = [Index(value = ["group_id", "origin", "value"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = DbGroup::class,
            parentColumns = ["entity_id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbGroupIdentifier(
    @ColumnInfo(name = "group_id") val groupId: Uuid,
    @ColumnInfo(name = "origin") val origin: EntityIdentifier.Origin,
    @ColumnInfo(name = "value") val value: String
) {
    fun toModel(): EntityIdentifier {
        return EntityIdentifier(
            entityId = groupId,
            origin = origin,
            value = value
        )
    }
}