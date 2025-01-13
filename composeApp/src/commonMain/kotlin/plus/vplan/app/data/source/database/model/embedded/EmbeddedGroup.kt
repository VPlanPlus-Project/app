package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchoolGroup
import plus.vplan.app.domain.model.Group

data class EmbeddedGroup(
    @Embedded val group: DbGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "group_id",
        entity = FKSchoolGroup::class
    ) val school: FKSchoolGroup,
) {
    fun toModel(): Group {
        return Group(
            id = group.id,
            name = group.name,
            school = school.schoolId
        )
    }
}