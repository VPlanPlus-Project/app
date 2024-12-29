package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.domain.model.Course

data class EmbeddedCourse(
    @Embedded val course: DbCourse,
    @Relation(
        parentColumn = "teacher_id",
        entityColumn = "id",
        entity = DbTeacher::class
    ) val teacher: EmbeddedTeacher?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DbCourseGroupCrossover::class,
            parentColumn = "course_id",
            entityColumn = "group_id"
        ),
        entity = DbGroup::class
    ) val groups: List<EmbeddedGroup>
) {
    fun toModel(): Course {
        return Course(
            id = course.id,
            name = course.name,
            teacher = teacher?.toModel(),
            groups = groups.map { it.toModel() }
        )
    }
}
