package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate

sealed interface Lesson {
    val id: String
    val date: LocalDate
    val week: Week
    val subject: String?
    val teachers: List<Teacher>
    val rooms: List<Room>?
    val groups: List<Group>
    val defaultLesson: DefaultLesson?
    val lessonTime: LessonTime

    data class TimetableLesson(
        override val id: String,
        override val date: LocalDate,
        override val week: Week,
        override val subject: String?,
        override val teachers: List<Teacher>,
        override val rooms: List<Room>?,
        override val groups: List<Group>,
        override val lessonTime: LessonTime
    ) : Lesson {
        override val defaultLesson: DefaultLesson? = null

        constructor(
            date: LocalDate,
            week: Week,
            subject: String?,
            teachers: List<Teacher>,
            rooms: List<Room>?,
            groups: List<Group>,
            lessonTime: LessonTime,
        ) : this(
            id = "$date.${groups.joinToString("|") { it.id.toString() }}.${lessonTime.lessonNumber}.$subject".lowercase(),
            date = date,
            week = week,
            subject = subject,
            teachers = teachers,
            rooms = rooms,
            groups = groups,
            lessonTime = lessonTime
        )
    }

    data class SubstitutionPlanLesson(
        override val id: String,
        override val date: LocalDate,
        override val week: Week,
        override val subject: String?,
        override val teachers: List<Teacher>,
        override val rooms: List<Room>,
        val isRoomChanged: Boolean,
        override val groups: List<Group>,
        override val defaultLesson: DefaultLesson?,
        override val lessonTime: LessonTime,
    ) : Lesson
}