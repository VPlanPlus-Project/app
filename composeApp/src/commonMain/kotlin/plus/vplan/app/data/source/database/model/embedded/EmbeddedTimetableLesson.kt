package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTimetable
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.DbTimetableWeekLimitation
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.domain.model.Lesson

data class EmbeddedTimetableLesson(
    @Embedded val timetableLesson: DbTimetableLesson,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "timetable_lesson_id",
            entityColumn = "teacher_id",
            value = DbTimetableTeacherCrossover::class
        ),
        entity = DbTeacher::class
    ) val teachers: List<EmbeddedTeacher>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "timetable_lesson_id",
            entityColumn = "room_id",
            value = DbTimetableRoomCrossover::class
        ),
        entity = DbRoom::class
    ) val rooms: List<EmbeddedRoom>,
    @Relation(
        parentColumn = "id",
        entityColumn = "timetable_lesson_id",
        entity = DbTimetableGroupCrossover::class
    ) val groups: List<DbTimetableGroupCrossover>,
    @Relation(
        parentColumn = "id",
        entityColumn = "timetable_lesson_id",
        entity = DbTimetableWeekLimitation::class
    ) val weekLimitations: List<DbTimetableWeekLimitation>,
    @Relation(
        parentColumn = "timetable_id",
        entityColumn = "id",
        entity = DbTimetable::class
    ) val timetable: DbTimetable,
) {
    fun toModel(): Lesson.TimetableLesson {
        return Lesson.TimetableLesson(
            id = timetableLesson.id,
            dayOfWeek = timetableLesson.dayOfWeek,
            weekId = timetable.weekId,
            subject = timetableLesson.subject,
            teachers = teachers.map { it.toModel() },
            rooms = rooms.map { it.toModel() },
            groupIds = groups.map { it.groupId },
            timetableId = timetableLesson.timetableId,
            weekType = timetableLesson.weekType,
            lessonNumber = timetableLesson.lessonNumber,
            limitedToWeekIds = weekLimitations.map { it.weekId }.toSet().ifEmpty { null }
        )
    }
}