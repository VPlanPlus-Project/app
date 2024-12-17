package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.domain.model.DefaultLesson

data class EmbeddedDefaultLesson(
    @Embedded val defaultLesson: DbDefaultLesson,
    @Relation(
        parentColumn = "teacher_id",
        entityColumn = "id",
        entity = DbTeacher::class
    ) val teacher: EmbeddedTeacher?,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        entity = DbGroup::class
    ) val group: EmbeddedGroup,
    @Relation(
        parentColumn = "course_id",
        entityColumn = "id",
        entity = DbCourse::class
    ) val course: EmbeddedCourse?
) {
    fun toModel(): DefaultLesson {
        return DefaultLesson(
            id = defaultLesson.id,
            subject = defaultLesson.subject,
            teacher = teacher?.toModel(),
            group = group.toModel(),
            course = course?.toModel()
        )
    }
}