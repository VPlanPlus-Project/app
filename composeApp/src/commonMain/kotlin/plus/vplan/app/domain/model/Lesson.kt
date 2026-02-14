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
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.DataTag
import plus.vplan.app.domain.data.Item
import plus.vplan.app.domain.repository.LessonTimeRepository
import kotlin.uuid.Uuid

sealed interface Lesson : Item<Uuid, DataTag> {
    val weekId: String?
    val subject: String?
    val teachers: List<Teacher>
    val rooms: List<Room>?
    val groupIds: List<Uuid>
    val subjectInstanceId: Uuid?
    val lessonNumber: Int

    fun getLessonSignature(): String

    override val tags: Set<DataTag>
        get() = emptySet()

    val lessonTime: Flow<CacheState<LessonTime>>?
    val subjectInstance: Flow<AliasState<SubjectInstance>>?
    val groups: Flow<List<AliasState<Group>>>

    val isCancelled: Boolean

    /**
     * @param weekId The ID of the week where the corresponding timetable starts to be valid.
     */
    data class TimetableLesson(
        override val id: Uuid,
        val dayOfWeek: DayOfWeek,
        override val weekId: String,
        override val subject: String?,
        override val teachers: List<Teacher>,
        override val rooms: List<Room>?,
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
        override val groups by lazy { if (groupIds.isEmpty()) flowOf(emptyList()) else combine(groupIds.map { App.groupSource.getById(it) }) { it.toList() } }

        val limitedToWeeks by lazy { if (limitedToWeekIds.isNullOrEmpty()) null else combine(limitedToWeekIds.map { App.weekSource.getById(it) }) { it.toList() } }

        override val subjectInstanceId = null
        override val isCancelled: Boolean = false

        override fun getLessonSignature(): String {
            return "$subject/${teachers.map { it.id }.sorted()}/${rooms.orEmpty().map { it.id }.sorted()}/$groupIds/$lessonNumber/$dayOfWeek/$weekType"
        }
    }

    data class SubstitutionPlanLesson(
        override val id: Uuid,
        val date: LocalDate,
        override val weekId: String?,
        override val subject: String?,
        val isSubjectChanged: Boolean,
        override val teachers: List<Teacher>,
        val isTeacherChanged: Boolean,
        override val rooms: List<Room>,
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
        override val groups by lazy { if (groupIds.isEmpty()) flowOf(emptyList()) else combine(groupIds.map { App.groupSource.getById(it) }) { it.toList() } }

        override val isCancelled: Boolean
            get() = subject == null && subjectInstanceId != null

        override fun getLessonSignature(): String {
            return "$subject/${teachers.map { it.id }.sorted()}/${rooms.map { it.id }.sorted()}/${groupIds.sorted()}/$lessonNumber/$date/$subjectInstanceId"
        }
    }

    suspend fun isRelevantForProfile(profile: Profile): Boolean {
        when (profile) {
            is Profile.StudentProfile -> {
                if (profile.group.id !in this.groupIds) return false
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
                if (profile.teacher.id !in this.teachers.map { it.id }) return false
            }
        }
        return true
    }
}