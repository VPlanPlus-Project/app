package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.domain.model.Course

data class EmbeddedCourse(
    @Embedded val course: DbCourse,
    @Relation(
        parentColumn = "teacher_id",
        entityColumn = "id",
        entity = DbTeacher::class
    ) val teacher: EmbeddedTeacher?,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        entity = DbGroup::class
    ) val group: EmbeddedGroup
) {
    fun toModel(): Course {
        return Course(
            id = course.id,
            name = course.name,
            teacher = teacher?.toModel(),
            group = group.toModel()
        )
    }
}
