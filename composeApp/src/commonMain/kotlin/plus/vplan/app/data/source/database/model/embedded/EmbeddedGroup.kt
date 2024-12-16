package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.domain.model.Group

data class EmbeddedGroup(
    @Embedded val group: DbGroup,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool,
) {
    fun toModel(): Group {
        return Group(
            id = group.id,
            name = group.name,
            school = school.toModel()
        )
    }
}