package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbDefaultLessonIdentifier
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
        entityColumn = "entity_id",
        entity = DbCourse::class
    ) val course: EmbeddedCourse?,
    @Relation(
        parentColumn = "entity_id",
        entityColumn = "default_lesson_id",
        entity = DbDefaultLessonIdentifier::class
    ) val identifiers: List<DbDefaultLessonIdentifier>
) {
    fun toModel(): DefaultLesson {
        return DefaultLesson(
            appId = defaultLesson.id,
            identifier = identifiers.map { it.toModel() },
            subject = defaultLesson.subject,
            teacher = teacher?.toModel(),
            group = group.toModel(),
            course = course?.toModel()
        )
    }
}