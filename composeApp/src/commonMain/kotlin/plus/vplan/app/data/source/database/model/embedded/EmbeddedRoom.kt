package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Room

data class EmbeddedRoom(
    @Embedded val room: DbRoom,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool,
) {
    fun toModel(): Room {
        return Room(
            id = room.id,
            school = Cacheable.Loaded(school.toModel()),
            name = room.name
        )
    }
}
