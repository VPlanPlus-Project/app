package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School

data class EmbeddedGroup(
    @Embedded val group: DbGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "group_id",
        entity = DbGroupAlias::class
    ) val aliases: List<DbGroupAlias>,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool
) {
    fun toModel(): Group {
        return Group(
            id = group.id,
            name = group.name,
            school = school.toModel() as School.AppSchool,
            cachedAt = group.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}