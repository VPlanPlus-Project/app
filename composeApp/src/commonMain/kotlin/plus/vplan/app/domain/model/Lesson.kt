package plus.vplan.app.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
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

    suspend fun getRoomItems(): List<Room>?
    val roomItems: List<Room>?

    suspend fun getTeacherItems(): List<Teacher>
    val teacherItems: List<Teacher>?

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
        override var roomItems: List<Room>? = null
            private set

        override var teacherItems: List<Teacher>? = null
            private set

        override var lessonTimeItem: LessonTime? = null
            private set

        override suspend fun getLessonTimeItem(): LessonTime {
            return lessonTimeItem ?: App.lessonTimeSource.getSingleById(lessonTime)!!.also { lessonTimeItem = it }
        }

        override suspend fun getRoomItems(): List<Room>? {
            return roomItems ?: rooms?.mapNotNull { App.roomSource.getSingleById(it) }?.also { roomItems = it }
        }

        override suspend fun getTeacherItems(): List<Teacher> {
            return teacherItems ?: teachers.mapNotNull { App.teacherSource.getSingleById(it) }.also { teacherItems = it }
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

        override var roomItems: List<Room>? = null
            private set

        override var teacherItems: List<Teacher>? = null
            private set

        override suspend fun getLessonTimeItem(): LessonTime {
            return lessonTimeItem ?: App.lessonTimeSource.getSingleById(lessonTime)!!.also { lessonTimeItem = it }
        }

        override suspend fun getRoomItems(): List<Room> {
            return roomItems ?: rooms.mapNotNull { App.roomSource.getSingleById(it) }.also { roomItems = it }
        }

        override suspend fun getTeacherItems(): List<Teacher> {
            return teacherItems ?: teachers.mapNotNull { App.teacherSource.getSingleById(it) }.also { teacherItems = it }
        }
    }

    suspend fun isRelevantForProfile(profile: Profile): Boolean {
        when (profile) {
            is Profile.StudentProfile -> {
                if (profile.group !in this.groups) return false
                if (profile.defaultLessons.filterValues { false }.any { it.key == this.defaultLesson }) return false
                if (this is TimetableLesson) {
                    val defaultLessons = profile.defaultLessons.mapKeys { profile.getDefaultLesson(it.key) }
                    if (defaultLessons.filterValues { !it }.any { it.key.getCourseItem()?.name == this.subject }) return false
                    if (defaultLessons.filterValues { !it }.any { it.key.course == null && it.key.subject == this.subject }) return false
                    defaultLessons.isEmpty()
                } else if (this is SubstitutionPlanLesson) {
                    if (this.defaultLesson != null && this.defaultLesson in profile.defaultLessons.filterValues { !it }) return false
                }
            }
            is Profile.TeacherProfile -> {
                if (profile.teacher !in this.teachers) return false
            }
            is Profile.RoomProfile -> {
                if (profile.room !in this.rooms.orEmpty()) return false
            }
        }
        return true
    }
}