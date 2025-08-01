package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherAlias
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.IndiwareBaseData

data class EmbeddedTeacher(
    @Embedded val teacher: DbTeacher,
    @Relation(
        parentColumn = "id",
        entityColumn = "teacher_id",
        entity = DbTeacherAlias::class
    ) val aliases: List<DbTeacherAlias>
) {
    fun toModel(): Teacher {
        return Teacher(
            id = teacher.id,
            name = teacher.name,
            schoolId = teacher.schoolId,
            cachedAt = teacher.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}