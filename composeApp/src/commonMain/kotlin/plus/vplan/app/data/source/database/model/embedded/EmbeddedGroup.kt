package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupIdentifier
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.domain.model.Group

data class EmbeddedGroup(
    @Embedded val group: DbGroup,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool,
    @Relation(
        parentColumn = "entity_id",
        entityColumn = "group_id",
        entity = DbGroupIdentifier::class
    ) val identifiers: List<DbGroupIdentifier>
) {
    fun toModel(): Group {
        return Group(
            appId = group.id,
            identifiers = identifiers.map { it.toModel() },
            name = group.name,
            school = school.toModel()
        )
    }
}