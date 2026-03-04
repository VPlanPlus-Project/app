package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbGroup
import plus.vplan.app.core.database.model.database.DbLessonTime
import plus.vplan.app.core.database.model.database.DbRoom
import plus.vplan.app.core.database.model.database.DbTeacher
import plus.vplan.app.core.database.model.database.DbTimetable
import plus.vplan.app.core.database.model.database.DbTimetableLesson
import plus.vplan.app.core.database.model.database.DbTimetableWeekLimitation
import plus.vplan.app.core.database.model.database.DbWeek
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.core.model.Lesson

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
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "timetable_lesson_id",
            entityColumn = "group_id",
            value = DbTimetableGroupCrossover::class,
        ),
        entity = DbGroup::class
    ) val groups: List<EmbeddedGroup>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "timetable_lesson_id",
            entityColumn = "week_id",
            value = DbTimetableWeekLimitation::class
        ),
        entity = DbWeek::class
    ) val weekLimitations: List<DbWeek>,
    @Relation(
        parentColumn = "timetable_id",
        entityColumn = "id",
        entity = DbTimetable::class
    ) val timetable: DbTimetable,
    @Relation(
        parentColumn = "lesson_time_id",
        entityColumn = "id",
        entity = DbLessonTime::class
    ) val lessonTime: DbLessonTime?,
) {
    fun toModel(): Lesson.TimetableLesson {
        return Lesson.TimetableLesson(
            id = timetableLesson.id,
            dayOfWeek = timetableLesson.dayOfWeek,
            weekId = timetable.weekId,
            subject = timetableLesson.subject,
            teachers = teachers.map { it.toModel() },
            rooms = rooms.map { it.toModel() },
            groups = groups.map { it.toModel() },
            timetableId = timetableLesson.timetableId,
            weekType = timetableLesson.weekType,
            lessonNumber = timetableLesson.lessonNumber,
            limitedToWeeks = weekLimitations.map { it.toModel() }.ifEmpty { null },
            lessonTime = lessonTime?.toModel(),
        )
    }
}