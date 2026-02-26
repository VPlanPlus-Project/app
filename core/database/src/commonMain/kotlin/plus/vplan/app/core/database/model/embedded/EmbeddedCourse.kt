package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbCourse
import plus.vplan.app.core.database.model.database.DbCourseAlias
import plus.vplan.app.core.database.model.database.DbGroup
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
    ) val aliases: List<DbCourseAlias>
) {
    fun toModel(): Course {
        return Course(
            id = course.id,
            name = course.name,
            teacherId = course.teacherId,
            groups = groups.flatMap { it.aliases.map { alias -> alias.toModel() } },
            cachedAt = course.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}
