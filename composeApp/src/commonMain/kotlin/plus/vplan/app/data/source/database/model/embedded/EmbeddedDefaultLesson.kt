package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbDefaultLessonGroupCrossover
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.DefaultLesson

data class EmbeddedDefaultLesson(
    @Embedded val defaultLesson: DbDefaultLesson,
    @Relation(
        parentColumn = "id",
        entityColumn = "default_lesson_id",
        entity = DbDefaultLessonGroupCrossover::class
    ) val groups: List<DbDefaultLessonGroupCrossover>
) {
    fun toModel(): DefaultLesson {
        return DefaultLesson(
            id = defaultLesson.id,
            subject = defaultLesson.subject,
            teacher = defaultLesson.teacherId?.let { Cacheable.Uninitialized(it.toString()) },
            groups = groups.map { Cacheable.Uninitialized(it.groupId.toString()) },
            course = defaultLesson.courseId?.let { Cacheable.Uninitialized(it) }
        )
    }
}