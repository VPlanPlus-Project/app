package plus.vplan.app.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

sealed interface Lesson {
    val id: String
    val week: Week
    val subject: String?
    val teachers: List<Teacher>
    val rooms: List<Room>?
    val groups: List<Group>
    val defaultLesson: DefaultLesson?
    val lessonTime: LessonTime

    data class TimetableLesson(
        override val id: String,
        val dayOfWeek: DayOfWeek,
        override val week: Week,
        override val subject: String?,
        override val teachers: List<Teacher>,
        override val rooms: List<Room>?,
        override val groups: List<Group>,
        override val lessonTime: LessonTime,
        val weekType: String?
    ) : Lesson {
        override val defaultLesson: DefaultLesson? = null

        constructor(
            dayOfWeek: DayOfWeek,
            week: Week,
            subject: String?,
            teachers: List<Teacher>,
            rooms: List<Room>?,
            groups: List<Group>,
            lessonTime: LessonTime,
            weekType: String?
        ) : this(
            id = Uuid.random().toHexString(),
            dayOfWeek = dayOfWeek,
            week = week,
            subject = subject,
            teachers = teachers,
            rooms = rooms,
            groups = groups,
            lessonTime = lessonTime,
            weekType = weekType
        )
    }

    data class SubstitutionPlanLesson(
        override val id: String,
        val date: LocalDate,
        override val week: Week,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teachers: List<Teacher>,
        val isTeacherChanged: Boolean,
        override val rooms: List<Room>,
        val isRoomChanged: Boolean,
        override val groups: List<Group>,
        override val defaultLesson: DefaultLesson?,
        override val lessonTime: LessonTime,
        val info: String?
    ) : Lesson
}