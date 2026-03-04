package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbCourse
import plus.vplan.app.core.database.model.database.DbCourseAlias
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbTeacher
import plus.vplan.app.core.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.core.model.Course

data class EmbeddedCourse(
    @Embedded val course: DbCourse,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = DbGroup::class,
        associateBy = Junction(
            value = DbCourseGroupCrossover::class,
            parentColumn = "course_id",
            entityColumn = "group_id"
        )
    ) val groups: List<EmbeddedGroup>,
    @Relation(
        parentColumn = "id",
        entityColumn = "course_id",
        entity = DbCourseAlias::class
    ) val aliases: List<DbCourseAlias>,
    @Relation(
        parentColumn = "teacher_id",
        entityColumn = "id",
        entity = DbTeacher::class,
    ) val teacher: EmbeddedTeacher?,
) {
    fun toModel(): Course {
        return Course(
            id = course.id,
            name = course.name,
            teacher = teacher?.toModel(),
            groups = groups.map { it.toModel() }.toSet(),
            cachedAt = course.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}
