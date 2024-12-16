package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.EntityIdentifier
import kotlin.uuid.Uuid

@Entity(
    tableName = "school_rooms",
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
data class DbRoom(
    @ColumnInfo(name = "entity_id") val id: Uuid,
    @ColumnInfo(name = "school_id") val schoolId: Int,
    @ColumnInfo(name = "name") val name: String
)

@Entity(
    tableName = "room_identifiers",
    primaryKeys = ["room_id", "origin", "value"],
    indices = [Index(value = ["room_id", "origin", "value"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = DbRoom::class,
            parentColumns = ["entity_id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbRoomIdentifier(
    @ColumnInfo(name = "room_id") val roomId: Uuid,
    @ColumnInfo(name = "origin") val origin: EntityIdentifier.Origin,
    @ColumnInfo(name = "value") val value: String
) {
    fun toModel(): EntityIdentifier {
        return EntityIdentifier(
            entityId = roomId,
            origin = origin,
            value = value
        )
    }
}