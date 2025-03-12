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
    val subjectInstance: Int?
    val lessonTime: String
    val version: String

    fun getLessonSignature(): String

    override fun getEntityId(): String = this.id.toHexString()

    suspend fun getLessonTimeItem(): LessonTime
    val lessonTimeItem: LessonTime?

    suspend fun getRoomItems(): List<Room>?
    val roomItems: List<Room>?

    suspend fun getTeacherItems(): List<Teacher>
    val teacherItems: List<Teacher>?

    suspend fun getGroupItems(): List<Group>
    val groupItems: List<Group>?

    val isCancelled: Boolean

    data class TimetableLesson(
        override val id: Uuid,
        val dayOfWeek: DayOfWeek,
        override val week: String,
        override val subject: String?,
        override val teachers: List<Int>,
        override val rooms: List<Int>?,
        override val groups: List<Int>,
        override val lessonTime: String,
        override val version: String,
        val weekType: String?
    ) : Lesson {
        override val subjectInstance = null
        override val isCancelled: Boolean = false
        override var roomItems: List<Room>? = null
            private set

        override var teacherItems: List<Teacher>? = null
            private set

        override var lessonTimeItem: LessonTime? = null
            private set

        override var groupItems: List<Group>? = null
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

        override suspend fun getGroupItems(): List<Group> {
            return groupItems ?: groups.mapNotNull { App.groupSource.getSingleById(it) }.also { groupItems = it }
        }

        override fun getLessonSignature(): String {
            return "$subject/$teachers/$rooms/$groups/$lessonTime/$dayOfWeek/$weekType"
        }

        constructor(
            dayOfWeek: DayOfWeek,
            week: String,
            subject: String?,
            teachers: List<Int>,
            rooms: List<Int>?,
            groups: List<Int>,
            lessonTime: String,
            version: String,
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
            weekType = weekType,
            version = version
        )
    }

    data class SubstitutionPlanLesson(
        override val id: Uuid,
        override val version: String,
        val date: LocalDate,
        override val week: String,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teachers: List<Int>,
        val isTeacherChanged: Boolean,
        override val rooms: List<Int>,
        val isRoomChanged: Boolean,
        override val groups: List<Int>,
        override val subjectInstance: Int?,
        override val lessonTime: String,
        val info: String?
    ) : Lesson {
        override val isCancelled: Boolean
            get() = subject == null && subjectInstance != null

        override var lessonTimeItem: LessonTime? = null
            private set

        override var roomItems: List<Room>? = null
            private set

        override var teacherItems: List<Teacher>? = null
            private set

        var subjectInstanceItem: SubjectInstance? = null
            private set

        override var groupItems: List<Group>? = null
            private set

        suspend fun getSubjectInstance(): SubjectInstance? {
            return subjectInstanceItem ?: if (subjectInstance == null) null else App.subjectInstanceSource.getSingleById(subjectInstance).also { subjectInstanceItem = it }
        }

        override fun getLessonSignature(): String {
            return "$subject/$teachers/$rooms/$groups/$lessonTime/$date/$subjectInstance"
        }

        override suspend fun getLessonTimeItem(): LessonTime {
            return lessonTimeItem ?: App.lessonTimeSource.getSingleById(lessonTime)!!.also { lessonTimeItem = it }
        }

        override suspend fun getRoomItems(): List<Room> {
            return roomItems ?: rooms.mapNotNull { App.roomSource.getSingleById(it) }.also { roomItems = it }
        }

        override suspend fun getTeacherItems(): List<Teacher> {
            return teacherItems ?: teachers.mapNotNull { App.teacherSource.getSingleById(it) }.also { teacherItems = it }
        }

        override suspend fun getGroupItems(): List<Group> {
            return groupItems ?: groups.mapNotNull { App.groupSource.getSingleById(it) }.also { groupItems = it }
        }
    }

    suspend fun isRelevantForProfile(profile: Profile): Boolean {
        when (profile) {
            is Profile.StudentProfile -> {
                if (profile.group !in this.groups) return false
                if (profile.subjectInstanceConfiguration.filterValues { false }.any { it.key == this.subjectInstance }) return false
                if (this is TimetableLesson) {
                    val subjectInstances = profile.subjectInstanceConfiguration.mapKeys { profile.getSubjectInstance(it.key) }
                    if (subjectInstances.filterValues { !it }.any { it.key.getCourseItem()?.name == this.subject }) return false
                    if (subjectInstances.filterValues { !it }.any { it.key.course == null && it.key.subject == this.subject }) return false
                    subjectInstances.isEmpty()
                } else if (this is SubstitutionPlanLesson) {
                    if (this.subjectInstance != null && this.subjectInstance in profile.subjectInstanceConfiguration.filterValues { !it }) return false
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