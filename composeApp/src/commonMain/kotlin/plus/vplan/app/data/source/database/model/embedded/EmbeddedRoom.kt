package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbRoomIdentifier
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.domain.model.Room

data class EmbeddedRoom(
    @Embedded val room: DbRoom,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool,
    @Relation(
        parentColumn = "entity_id",
        entityColumn = "room_id",
        entity = DbRoomIdentifier::class
    ) val identifiers: List<DbRoomIdentifier>
) {
    fun toModel(): Room {
        return Room(
            appId = room.id,
            identifiers = identifiers.map { it.toModel() },
            school = school.toModel(),
            name = room.name
        )
    }
}
