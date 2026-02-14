package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.core.model.Group

data class EmbeddedGroup(
    @Embedded val group: DbGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "group_id",
        entity = DbGroupAlias::class
    ) val aliases: List<DbGroupAlias>
) {
    fun toModel(): Group {
        return Group(
            id = group.id,
            name = group.name,
            schoolId = group.schoolId,
            cachedAt = group.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}