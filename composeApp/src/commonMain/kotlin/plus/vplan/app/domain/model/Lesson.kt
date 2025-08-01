package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.cache.Item
import kotlin.uuid.Uuid

sealed interface Lesson : Item<DataTag> {
    val id: Uuid
    val week: String
    val subject: String?
    val teacherIds: List<Uuid>
    val roomIds: List<Uuid>?
    val groupIds: List<Uuid>
    val subjectInstanceId: Int?
    val lessonTimeId: String

    fun getLessonSignature(): String

    override fun getEntityId(): String = this.id.toHexString()
    override val tags: Set<DataTag>
        get() = emptySet()

    val lessonTime: Flow<CacheStateOld<LessonTime>>
    val subjectInstance: Flow<CacheStateOld<SubjectInstance>>?
    val rooms: Flow<List<CacheState<Room>>>
    val groups: Flow<List<CacheState<Group>>>
    val teachers: Flow<List<CacheState<Teacher>>>

    val isCancelled: Boolean

    data class TimetableLesson(
        override val id: Uuid,
        val dayOfWeek: DayOfWeek,
        override val week: String,
        override val subject: String?,
        override val teacherIds: List<Uuid>,
        override val roomIds: List<Uuid>?,
        override val groupIds: List<Uuid>,
        override val lessonTimeId: String,
        val weekType: String?
    ) : Lesson {
        override val lessonTime by lazy { App.lessonTimeSource.getById(lessonTimeId) }
        override val subjectInstance = null
        override val rooms by lazy { if (roomIds.isNullOrEmpty()) flowOf(emptyList()) else combine(roomIds.map { App.roomSource.getById(it) }) { it.toList() } }
        override val groups by lazy { if (groupIds.isEmpty()) flowOf(emptyList()) else combine(groupIds.map { App.groupSource.getById(it) }) { it.toList() } }
        override val teachers by lazy { if (teacherIds.isEmpty()) flowOf(emptyList()) else combine(teacherIds.map { App.teacherSource.getById(it) }) { it.toList() } }

        override val subjectInstanceId = null
        override val isCancelled: Boolean = false

        override fun getLessonSignature(): String {
            return "$subject/$teacherIds/$roomIds/$groupIds/$lessonTimeId/$dayOfWeek/$weekType"
        }

        constructor(
            dayOfWeek: DayOfWeek,
            week: String,
            subject: String?,
            teachers: List<Uuid>,
            rooms: List<Uuid>?,
            groups: List<Uuid>,
            lessonTime: String,
            weekType: String?
        ) : this(
            id = Uuid.random(),
            dayOfWeek = dayOfWeek,
            week = week,
            subject = subject,
            teacherIds = teachers,
            roomIds = rooms,
            groupIds = groups,
            lessonTimeId = lessonTime,
            weekType = weekType,
        )
    }

    data class SubstitutionPlanLesson(
        override val id: Uuid,
        val date: LocalDate,
        override val week: String,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teacherIds: List<Uuid>,
        val isTeacherChanged: Boolean,
        override val roomIds: List<Uuid>,
        val isRoomChanged: Boolean,
        override val groupIds: List<Uuid>,
        override val subjectInstanceId: Int?,
        override val lessonTimeId: String,
        val info: String?
    ) : Lesson {
        override val lessonTime by lazy { App.lessonTimeSource.getById(lessonTimeId) }
        override val subjectInstance by lazy { if (subjectInstanceId == null) null else App.subjectInstanceSource.getById(subjectInstanceId) }
        override val rooms by lazy { if (roomIds.isEmpty()) flowOf(emptyList()) else combine(roomIds.map { App.roomSource.getById(it) }) { it.toList() } }
        override val groups by lazy { if (groupIds.isEmpty()) flowOf(emptyList()) else combine(groupIds.map { App.groupSource.getById(it) }) { it.toList() } }
        override val teachers by lazy { if (teacherIds.isEmpty()) flowOf(emptyList()) else combine(teacherIds.map { App.teacherSource.getById(it) }) { it.toList() } }

        override val isCancelled: Boolean
            get() = subject == null && subjectInstanceId != null

        override fun getLessonSignature(): String {
            return "$subject/${teacherIds.sorted()}/${roomIds.sorted()}/${groupIds.sorted()}/$lessonTimeId/$date/$subjectInstanceId"
        }
    }

    suspend fun isRelevantForProfile(profile: Profile): Boolean {
        when (profile) {
            is Profile.StudentProfile -> {
                if (profile.groupId !in this.groupIds) return false
                if (profile.subjectInstanceConfiguration.filterValues { false }.any { it.key == this.subjectInstanceId }) return false
                if (this is TimetableLesson) {
                    val subjectInstances = profile.subjectInstanceConfiguration.mapKeys { profile.getSubjectInstance(it.key) }
                    if (subjectInstances.filterValues { !it }.any { it.key.getCourseItem()?.name == this.subject }) return false
                    if (subjectInstances.filterValues { !it }.any { it.key.course == null && it.key.subject == this.subject }) return false
                    subjectInstances.isEmpty()
                } else if (this is SubstitutionPlanLesson) {
                    if (this.subjectInstanceId != null && this.subjectInstanceId in profile.subjectInstanceConfiguration.filterValues { !it }) return false
                }
            }
            is Profile.TeacherProfile -> {
                if (profile.teacher !in this.teacherIds) return false
            }
            is Profile.RoomProfile -> {
                if (profile.room !in this.roomIds.orEmpty()) return false
            }
        }
        return true
    }
}