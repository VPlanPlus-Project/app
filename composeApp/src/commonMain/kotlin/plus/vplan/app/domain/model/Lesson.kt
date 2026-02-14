package plus.vplan.app.domain.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.App
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Item
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.domain.repository.LessonTimeRepository
import kotlin.uuid.Uuid

sealed interface Lesson : Item<Uuid, DataTag> {
    val weekId: String?
    val subject: String?
    val teacherIds: List<Uuid>
    val roomIds: List<Uuid>?
    val groupIds: List<Uuid>
    val subjectInstanceId: Uuid?
    val lessonNumber: Int

    fun getLessonSignature(): String

    override val tags: Set<DataTag>
        get() = emptySet()

    val lessonTime: Flow<CacheState<LessonTime>>?
    val subjectInstance: Flow<AliasState<SubjectInstance>>?
    val rooms: Flow<List<AliasState<Room>>>
    val groups: Flow<List<AliasState<Group>>>
    val teachers: Flow<List<AliasState<Teacher>>>

    val isCancelled: Boolean

    /**
     * @param weekId The ID of the week where the corresponding timetable starts to be valid.
     */
    data class TimetableLesson(
        override val id: Uuid,
        val dayOfWeek: DayOfWeek,
        override val weekId: String,
        override val subject: String?,
        override val teacherIds: List<Uuid>,
        override val roomIds: List<Uuid>?,
        override val groupIds: List<Uuid>,
        override val lessonNumber: Int,
        val timetableId: Uuid,
        val weekType: String?,
        val limitedToWeekIds: Set<String>?
    ) : Lesson, KoinComponent {
        private val lessonTimeRepository by inject<LessonTimeRepository>()

        override val lessonTime by lazy {
            if (groupIds.isEmpty()) null
            else lessonTimeRepository.get(groupIds.first(), lessonNumber)
                .map { it?.let { CacheState.Done(it) } ?: CacheState.NotExisting() }
        }
        override val subjectInstance = null
        override val rooms by lazy { if (roomIds.isNullOrEmpty()) flowOf(emptyList()) else combine(roomIds.map { App.roomSource.getById(it) }) { it.toList() } }
        override val groups by lazy { if (groupIds.isEmpty()) flowOf(emptyList()) else combine(groupIds.map { App.groupSource.getById(it) }) { it.toList() } }
        override val teachers by lazy { if (teacherIds.isEmpty()) flowOf(emptyList()) else combine(teacherIds.map { App.teacherSource.getById(it) }) { it.toList() } }

        val limitedToWeeks by lazy { if (limitedToWeekIds.isNullOrEmpty()) null else combine(limitedToWeekIds.map { App.weekSource.getById(it) }) { it.toList() } }

        override val subjectInstanceId = null
        override val isCancelled: Boolean = false

        override fun getLessonSignature(): String {
            return "$subject/$teacherIds/$roomIds/$groupIds/$lessonNumber/$dayOfWeek/$weekType"
        }
    }

    data class SubstitutionPlanLesson(
        override val id: Uuid,
        val date: LocalDate,
        override val weekId: String?,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teacherIds: List<Uuid>,
        val isTeacherChanged: Boolean,
        override val roomIds: List<Uuid>,
        val isRoomChanged: Boolean,
        override val groupIds: List<Uuid>,
        override val subjectInstanceId: Uuid?,
        override val lessonNumber: Int,
        val info: String?
    ) : Lesson, KoinComponent {
        private val lessonTimeRepository by inject<LessonTimeRepository>()

        override val lessonTime by lazy {
            if (groupIds.isEmpty()) null
            else lessonTimeRepository.get(groupIds.first(), lessonNumber)
                .map { it?.let { CacheState.Done(it) } ?: CacheState.NotExisting() }
        }
        override val subjectInstance by lazy { if (subjectInstanceId == null) null else App.subjectInstanceSource.getById(subjectInstanceId) }
        override val rooms by lazy { if (roomIds.isEmpty()) flowOf(emptyList()) else combine(roomIds.map { App.roomSource.getById(it) }) { it.toList() } }
        override val groups by lazy { if (groupIds.isEmpty()) flowOf(emptyList()) else combine(groupIds.map { App.groupSource.getById(it) }) { it.toList() } }
        override val teachers by lazy { if (teacherIds.isEmpty()) flowOf(emptyList()) else combine(teacherIds.map { App.teacherSource.getById(it) }) { it.toList() } }

        override val isCancelled: Boolean
            get() = subject == null && subjectInstanceId != null

        override fun getLessonSignature(): String {
            return "$subject/${teacherIds.sorted()}/${roomIds.sorted()}/${groupIds.sorted()}/$lessonNumber/$date/$subjectInstanceId"
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
                    if (subjectInstances.filterValues { !it }.any { it.key.courseId == null && it.key.subject == this.subject }) return false
                    subjectInstances.isEmpty()
                } else if (this is SubstitutionPlanLesson) {
                    if (this.subjectInstanceId != null && this.subjectInstanceId in profile.subjectInstanceConfiguration.filterValues { !it }) return false
                }
            }
            is Profile.TeacherProfile -> {
                if (profile.teacher !in this.teacherIds) return false
            }
        }
        return true
    }
}