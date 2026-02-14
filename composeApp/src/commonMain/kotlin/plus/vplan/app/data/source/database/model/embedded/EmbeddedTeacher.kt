package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherAlias
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher

data class EmbeddedTeacher(
    @Embedded val teacher: DbTeacher,
    @Relation(
        parentColumn = "id",
        entityColumn = "teacher_id",
        entity = DbTeacherAlias::class
    ) val aliases: List<DbTeacherAlias>,
    @Relation(
        parentColumn = "school_id",
        entityColumn = "id",
        entity = DbSchool::class
    ) val school: EmbeddedSchool
) {
    fun toModel(): Teacher {
        return Teacher(
            id = teacher.id,
            name = teacher.name,
            school = school.toModel() as School.AppSchool,
            cachedAt = teacher.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}