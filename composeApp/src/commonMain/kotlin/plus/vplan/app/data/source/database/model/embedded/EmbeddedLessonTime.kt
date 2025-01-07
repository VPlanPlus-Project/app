package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.LessonTime

data class EmbeddedLessonTime(
    @Embedded val lessonTime: DbLessonTime,
    @Relation(
        parentColumn = "group_id",
        entityColumn = "id",
        entity = DbGroup::class
    ) val group: EmbeddedGroup
) {
    fun toModel() = LessonTime(
        id = lessonTime.id,
        start = lessonTime.startTime,
        end = lessonTime.endTime,
        lessonNumber = lessonTime.lessonNumber,
        group = Cacheable.Loaded(group.toModel()),
        interpolated = lessonTime.interpolated
    )
}
