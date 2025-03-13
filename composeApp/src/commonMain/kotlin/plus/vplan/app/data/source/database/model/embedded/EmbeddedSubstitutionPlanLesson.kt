package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.domain.model.Lesson

data class EmbeddedSubstitutionPlanLesson(
    @Embedded val substitutionPlanLesson: DbSubstitutionPlanLesson,
    @Relation(
        parentColumn = "id",
        entityColumn = "substitution_plan_lesson_id",
        entity = DbSubstitutionPlanTeacherCrossover::class
    ) val teachers: List<DbSubstitutionPlanTeacherCrossover>,
    @Relation(
        parentColumn = "id",
        entityColumn = "substitution_plan_lesson_id",
        entity = DbSubstitutionPlanRoomCrossover::class
    ) val rooms: List<DbSubstitutionPlanRoomCrossover>,
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
            week = day.weekId,
            subject = substitutionPlanLesson.subject,
            isSubjectChanged = substitutionPlanLesson.isSubjectChanged,
            teachers = teachers.map { it.teacherId },
            isTeacherChanged = substitutionPlanLesson.isTeacherChanged,
            rooms = rooms.map { it.roomId },
            isRoomChanged = substitutionPlanLesson.isRoomChanged,
            groups = groups.map { it.groupId },
            subjectInstance = substitutionPlanLesson.subjectInstanceId,
            lessonTimeId = substitutionPlanLesson.lessonTimeId,
            version = substitutionPlanLesson.version,
            info = substitutionPlanLesson.info
        )
    }
}