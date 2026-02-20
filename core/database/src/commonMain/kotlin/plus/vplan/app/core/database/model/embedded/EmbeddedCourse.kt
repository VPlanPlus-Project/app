package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbCourse
import plus.vplan.app.core.database.model.database.DbCourseAlias
import plus.vplan.app.core.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.core.model.Course

data class EmbeddedCourse(
    @Embedded val course: DbCourse,
    @Relation(
        parentColumn = "id",
        entityColumn = "course_id",
        entity = DbCourseGroupCrossover::class
    ) val groups: List<DbCourseGroupCrossover>,
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
            groupIds = groups.map { it.groupId },
            cachedAt = course.cachedAt,
            aliases = aliases.map { it.toModel() }.toSet()
        )
    }
}
