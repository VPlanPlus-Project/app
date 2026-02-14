package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.domain.model.Lesson

data class EmbeddedSubstitutionPlanLesson(
    @Embedded val substitutionPlanLesson: DbSubstitutionPlanLesson,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = DbTeacher::class,
        associateBy = Junction(
            value = DbSubstitutionPlanTeacherCrossover::class,
            parentColumn = "teacher_id",
            entityColumn = "substitution_plan_lesson_id"
        ),
    ) val teachers: List<EmbeddedTeacher>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = DbRoom::class,
        associateBy = Junction(
            value = DbSubstitutionPlanRoomCrossover::class,
            parentColumn = "room_id",
            entityColumn = "substitution_plan_lesson_id"
        ),
    ) val rooms: List<EmbeddedRoom>,
    @Relation(
        parentColumn = "id",
        entityColumn = "substitution_plan_lesson_id",
        entity = DbSubstitutionPlanGroupCrossover::class
    ) val groups: List<DbSubstitutionPlanGroupCrossover>,
    @Relation(
        parentColumn = "day_id",
        entityColumn = "id",
        entity = DbDay::class
    ) val day: DbDay
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
            groupIds = groups.map { it.groupId },
            subjectInstanceId = substitutionPlanLesson.subjectInstanceId,
            lessonNumber = substitutionPlanLesson.lessonNumber,
            info = substitutionPlanLesson.info
        )
    }
}