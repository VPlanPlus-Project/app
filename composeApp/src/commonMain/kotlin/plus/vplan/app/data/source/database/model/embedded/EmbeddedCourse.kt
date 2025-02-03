package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.domain.model.Course

data class EmbeddedCourse(
    @Embedded val course: DbCourse,
    @Relation(
        parentColumn = "id",
        entityColumn = "course_id",
        entity = DbCourseGroupCrossover::class
    ) val groups: List<DbCourseGroupCrossover>
) {
    fun toModel(): Course {
        return Course(
            id = course.id,
            indiwareId = course.indiwareId,
            name = course.name,
            teacher = course.teacherId,
            groups = groups.map { it.groupId }
        )
    }
}
