package plus.vplan.app.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue
import kotlin.uuid.Uuid

sealed interface Lesson : Item {
    val id: Uuid
    val week: String
    val subject: String?
    val teachers: List<Int>
    val rooms: List<Int>?
    val groups: List<Int>
    val defaultLesson: String?
    val lessonTime: String

    override fun getEntityId(): String = this.id.toHexString()

    suspend fun getLessonTimeItem(): LessonTime
    val lessonTimeItem: LessonTime?

    data class TimetableLesson(
        override val id: Uuid,
        val dayOfWeek: DayOfWeek,
        override val week: String,
        override val subject: String?,
        override val teachers: List<Int>,
        override val rooms: List<Int>?,
        override val groups: List<Int>,
        override val lessonTime: String,
        val weekType: String?
    ) : Lesson {
        override val defaultLesson = null

        override var lessonTimeItem: LessonTime? = null
            private set

        override suspend fun getLessonTimeItem(): LessonTime {
            return lessonTimeItem ?: App.lessonTimeSource.getById(lessonTime).getFirstValue().also { lessonTimeItem = it }
        }

        constructor(
            dayOfWeek: DayOfWeek,
            week: String,
            subject: String?,
            teachers: List<Int>,
            rooms: List<Int>?,
            groups: List<Int>,
            lessonTime: String,
            weekType: String?
        ) : this(
            id = Uuid.random(),
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
        override val id: Uuid,
        val date: LocalDate,
        override val week: String,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teachers: List<Int>,
        val isTeacherChanged: Boolean,
        override val rooms: List<Int>,
        val isRoomChanged: Boolean,
        override val groups: List<Int>,
        override val defaultLesson: String?,
        override val lessonTime: String,
        val info: String?
    ) : Lesson {
        override var lessonTimeItem: LessonTime? = null
            private set

        override suspend fun getLessonTimeItem(): LessonTime {
            return lessonTimeItem ?: App.lessonTimeSource.getById(lessonTime).getFirstValue().also { lessonTimeItem = it }
        }
    }
}