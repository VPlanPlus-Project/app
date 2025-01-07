package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Lesson

data class EmbeddedSubstitutionPlanLesson(
    @Embedded val substitutionPlanLesson: DbSubstitutionPlanLesson,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "substitution_plan_lesson_id",
            entityColumn = "teacher_id",
            value = DbSubstitutionPlanTeacherCrossover::class
        ),
        entity = DbTeacher::class
    ) val teachers: List<EmbeddedTeacher>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "substitution_plan_lesson_id",
            entityColumn = "room_id",
            value = DbSubstitutionPlanRoomCrossover::class
        ),
        entity = DbRoom::class
    ) val rooms: List<EmbeddedRoom>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "substitution_plan_lesson_id",
            entityColumn = "group_id",
            value = DbSubstitutionPlanGroupCrossover::class
        ),
        entity = DbGroup::class
    ) val groups: List<EmbeddedGroup>,
    @Relation(
        parentColumn = "lesson_time_id",
        entityColumn = "id",
        entity = DbLessonTime::class
    ) val lessonTime: EmbeddedLessonTime,
    @Relation(
        parentColumn = "default_lesson_id",
        entityColumn = "id",
        entity = DbDefaultLesson::class
    ) val defaultLesson: EmbeddedDefaultLesson?,
    @Relation(
        parentColumn = "day_id",
        entityColumn = "id",
        entity = DbDay::class
    ) val day: EmbeddedDay
) {
    fun toModel(): Lesson.SubstitutionPlanLesson {
        return Lesson.SubstitutionPlanLesson(
            id = substitutionPlanLesson.id,
            date = day.day.date,
            week = Cacheable.Loaded(day.week.toModel()),
            subject = substitutionPlanLesson.subject,
            isSubjectChanged = substitutionPlanLesson.isSubjectChanged,
            teachers = teachers.map { Cacheable.Loaded(it.toModel()) },
            isTeacherChanged = substitutionPlanLesson.isTeacherChanged,
            rooms = rooms.map { Cacheable.Loaded(it.toModel()) },
            isRoomChanged = substitutionPlanLesson.isRoomChanged,
            groups = groups.map { Cacheable.Loaded(it.toModel()) },
            defaultLesson = defaultLesson?.toModel()?.let { Cacheable.Loaded(it) },
            lessonTime = Cacheable.Loaded(lessonTime.toModel()),
            info = substitutionPlanLesson.info
        )
    }
}