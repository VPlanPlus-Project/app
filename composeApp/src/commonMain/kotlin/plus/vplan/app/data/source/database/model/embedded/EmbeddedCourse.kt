package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbCourseIdentifier
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.domain.model.Course

data class EmbeddedCourse(
    @Embedded val course: DbCourse,
    @Relation(
        parentColumn = "teacher_id",
        entityColumn = "entity_id",
        entity = DbTeacher::class
    ) val teacher: EmbeddedTeacher,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "entity_id",
        entity = DbGroup::class
    ) val group: EmbeddedGroup,
    @Relation(
        parentColumn = "entity_id",
        entityColumn = "course_id",
        entity = DbCourseIdentifier::class
    ) val identifiers: List<DbCourseIdentifier>
) {
    fun toModel(): Course {
        return Course(
            appId = course.id,
            identifiers = identifiers.map { it.toModel() },
            name = course.name,
            teacher = teacher.toModel(),
            group = group.toModel()
        )
    }
}
