package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbDay
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbLessonTime
import plus.vplan.app.core.database.model.database.DbRoom
import plus.vplan.app.core.database.model.database.DbSubjectInstance
import plus.vplan.app.core.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.core.database.model.database.DbTeacher
import plus.vplan.app.core.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.core.model.Lesson

data class EmbeddedSubstitutionPlanLesson(
    @Embedded val substitutionPlanLesson: DbSubstitutionPlanLesson,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DbSubstitutionPlanTeacherCrossover::class,
            parentColumn = "substitution_plan_lesson_id",
            entityColumn = "teacher_id",
        ),
        entity = DbTeacher::class
    ) val teachers: List<EmbeddedTeacher>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DbSubstitutionPlanRoomCrossover::class,
            parentColumn = "substitution_plan_lesson_id",
            entityColumn = "room_id"
        ),
        entity = DbRoom::class,
    ) val rooms: List<EmbeddedRoom>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DbSubstitutionPlanGroupCrossover::class,
            parentColumn = "substitution_plan_lesson_id",
            entityColumn = "group_id"
        ),
        entity = DbGroup::class
    ) val groups: List<EmbeddedGroup>,
    @Relation(
        parentColumn = "day_id",
        entityColumn = "id",
        entity = DbDay::class
    ) val day: DbDay,
    @Relation(
        parentColumn = "subject_instance_id",
        entityColumn = "id",
        entity = DbSubjectInstance::class,
    ) val subjectInstance: EmbeddedSubjectInstance?,
    @Relation(
        parentColumn = "lesson_time_id",
        entityColumn = "id",
        entity = DbLessonTime::class,
    ) val lessonTime: DbLessonTime?,
) {
    fun toModel(): Lesson.SubstitutionPlanLesson {
        return Lesson.SubstitutionPlanLesson(
            id = substitutionPlanLesson.id,
            date = day.date,
            weekId = day.weekId,
            subject = substitutionPlanLesson.subject,
            isSubjectChanged = substitutionPlanLesson.isSubjectChanged,
            teachers = teachers.map { it.toModel() },
            isTeacherChanged = substitutionPlanLesson.isTeacherChanged,
            rooms = rooms.map { it.toModel() },
            isRoomChanged = substitutionPlanLesson.isRoomChanged,
            groups = groups.map { it.toModel() },
            subjectInstance = subjectInstance?.toModel(),
            lessonNumber = substitutionPlanLesson.lessonNumber,
            info = substitutionPlanLesson.info,
            lessonTime = lessonTime?.toModel(),
        )
    }
}