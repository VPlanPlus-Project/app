package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.data.source.database.model.database.DbRoom
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
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
    ) val teachers: List<DbTeacher>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            parentColumn = "timetable_lesson_id",
            entityColumn = "room_id",
            value = DbTimetableRoomCrossover::class
        ),
        entity = DbRoom::class
    ) val rooms: List<DbRoom>,
    @Relation(
        parentColumn = "id",
        entityColumn = "timetable_lesson_id",
        entity = DbTimetableGroupCrossover::class
    ) val groups: List<DbTimetableGroupCrossover>,
    @Relation(
        parentColumn = "lesson_time_id",
        entityColumn = "id",
        entity = DbLessonTime::class
    ) val lessonTime: DbLessonTime
) {
    fun toModel(): Lesson.TimetableLesson {
        return Lesson.TimetableLesson(
            id = timetableLesson.id,
            dayOfWeek = timetableLesson.dayOfWeek,
            week = timetableLesson.weekId,
            subject = timetableLesson.subject,
            teacherIds = teachers.map { it.id },
            roomIds = rooms.map { it.id },
            groupIds = groups.map { it.groupId },
            lessonTimeId = timetableLesson.lessonTimeId,
            weekType = timetableLesson.weekType
        )
    }
}