package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomAlias
import plus.vplan.app.core.model.Room

data class EmbeddedRoom(
    @Embedded val room: DbRoom,
    @Relation(
        parentColumn = "id",
        entityColumn = "room_id",
        entity = DbRoomAlias::class
    ) val aliases: List<DbRoomAlias>
) {
    fun toModel(): Room {
        return Room(
            id = room.id,
            name = room.name,
            schoolId = room.schoolId,
            cachedAt = room.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}