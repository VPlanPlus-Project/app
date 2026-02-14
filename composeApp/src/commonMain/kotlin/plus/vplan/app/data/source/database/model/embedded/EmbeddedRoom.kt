package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomAlias
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.domain.model.Room

data class EmbeddedRoom(
    @Embedded val room: DbRoom,
    @Relation(
        parentColumn = "id",
        entityColumn = "room_id",
        entity = DbRoomAlias::class
    ) val aliases: List<DbRoomAlias>,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool,
) {
    fun toModel(): Room {
        return Room(
            id = room.id,
            name = room.name,
            school = school.toModel(),
            cachedAt = room.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}