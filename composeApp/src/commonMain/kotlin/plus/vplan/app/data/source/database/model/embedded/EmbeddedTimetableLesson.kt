package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.DbWeek
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
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "timetable_lesson_id",
            entityColumn = "group_id",
            value = DbTimetableGroupCrossover::class
        ),
        entity = DbGroup::class
    ) val groups: List<EmbeddedGroup>,
    @Relation(
        parentColumn = "week_id",
        entityColumn = "id",
        entity = DbWeek::class
    ) val week: EmbeddedWeek,
    @Relation(
        parentColumn = "lesson_time_id",
        entityColumn = "id",
        entity = DbLessonTime::class
    ) val lessonTime: EmbeddedLessonTime
) {
    fun toModel(): Lesson.TimetableLesson {
        val week = week.toModel()
        return Lesson.TimetableLesson(
            id = timetableLesson.id,
            dayOfWeek = timetableLesson.dayOfWeek,
            week = week,
            subject = timetableLesson.subject,
            teachers = teachers.map { it.toModel() },
            rooms = rooms.map { it.toModel() },
            groups = groups.map { it.toModel() },
            lessonTime = lessonTime.toModel(),
            weekType = timetableLesson.weekType
        )
    }
}