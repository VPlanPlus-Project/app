package plus.vplan.app.domain.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.App
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.DataTag
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Item
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class Homework(
    val creator: AppEntity
) : Item<Int, DataTag>, KoinComponent {
    override val tags: Set<DataTag> = emptySet()
    abstract val createdAt: Instant
    abstract val dueTo: LocalDate
    abstract val taskIds: List<Int>
    abstract val subjectInstanceId: Int?
    abstract val fileIds: List<Int>
    abstract val cachedAt: Instant

    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()

    val subjectInstance by lazy { this.subjectInstanceId?.let { subjectInstanceRepository.findByAlias(Alias(AliasProvider.Vpp, it.toString(), 1), forceUpdate = false, preferCurrentState = false) } }
    val tasks by lazy {
        if (taskIds.isEmpty()) flowOf(emptyList())
        else combine(taskIds.map { id -> App.homeworkTaskSource.getById(id).filterIsInstance<CacheState.Done<HomeworkTask>>().map { it.data } }) { it.toList() }
    }

    abstract val group: Flow<AliasState<Group>>?
    abstract val groupId: Int?

    var taskItems: List<HomeworkTask>? = null
        private set

    var fileItems: List<File>? = null
        private set

    fun getTasksFlow() = if (taskIds.isEmpty()) flowOf(emptyList()) else combine(taskIds.map { App.homeworkTaskSource.getById(it).filterIsInstance<CacheState.Done<HomeworkTask>>() }) { it.toList().map { it.data }.also { taskItems = it } }
    fun getStatusFlow(profile: Profile.StudentProfile) = getTasksFlow().map { tasks ->
        if (tasks.all { it.isDone(profile) }) HomeworkStatus.DONE
        else if (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date > dueTo) HomeworkStatus.OVERDUE
        else HomeworkStatus.PENDING
    }

    fun getFilesFlow() = if (fileIds.isEmpty()) flowOf(emptyList()) else combine(fileIds.map { App.fileSource.getById(it).filterIsInstance<CacheState.Done<File>>() }) { it.toList().map { it.data } }

    suspend fun getTaskItems(): List<HomeworkTask> {
        return taskItems ?: taskIds.mapNotNull { App.homeworkTaskSource.getSingleById(it) }.also { taskItems = it }
    }

    suspend fun getFileItems(): List<File> {
        if (fileIds.isEmpty()) return emptyList()
        return fileItems ?: combine(fileIds.map { App.fileSource.getById(it) }) { it.toList().mapNotNull { (it as? CacheState.Done<File>)?.data } }.first().also { fileItems = it }
    }

    data class HomeworkTask(
        override val id: Int,
        val content: String,
        val doneByProfiles: List<Uuid>,
        val doneByVppIds: List<Int>,
        val homework: Int,
        val cachedAt: Instant
    ) : Item<Int, DataTag> {
        override val tags: Set<DataTag> = emptySet()

        var homeworkItem: Homework? = null
            private set

        suspend fun getHomeworkItem(): Homework? {
            return homeworkItem ?: App.homeworkSource.getById(homework).getFirstValueOld().also { homeworkItem = it }
        }

        fun isDone(profile: Profile.StudentProfile) = (profile.id in doneByProfiles && profile.vppId == null) || profile.vppId?.id in doneByVppIds
    }

    data class HomeworkFile(
        override val id: Int,
        val name: String,
        val homework: Int,
        val size: Long,
    ) : Item<Int, DataTag> {
        override val tags: Set<DataTag> = emptySet()
    }

    data class CloudHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: LocalDate,
        override val taskIds: List<Int>,
        override val subjectInstanceId: Int?,
        override val fileIds: List<Int>,
        override val cachedAt: Instant,
        override val groupId: Int?,
        val isPublic: Boolean,
        val createdById: Int,
    ) : Homework(
        creator = AppEntity.VppId(createdById)
    ) {

        private val groupRepository by inject<GroupRepository>()
        private val vppIdRepository by inject<VppIdRepository>()

        override val group: Flow<AliasState<Group>>? = groupId?.let {
            groupRepository.findByAlias(Alias(AliasProvider.Vpp, it.toString(), 1), forceUpdate = false, preferCurrentState = false)
        }

        val createdBy by lazy {
            vppIdRepository.getById(createdById, ResponsePreference.Fast)
        }

        fun createdBy(responsePreference: ResponsePreference) = vppIdRepository.getById(createdById, responsePreference)
    }

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: LocalDate,
        override val taskIds: List<Int>,
        override val subjectInstanceId: Int?,
        override val groupId: Int?,
        override val fileIds: List<Int>,
        override val cachedAt: Instant,
        val createdByProfileId: Uuid
    ) : Homework(
        creator = AppEntity.Profile(createdByProfileId)
    ) {
        @OptIn(ExperimentalCoroutinesApi::class)
        override val group: Flow<AliasState<Group>> by lazy {
            App.profileSource.getById(createdByProfileId)
                .filterIsInstance<CacheState.Done<Profile.StudentProfile>>()
                .map { AliasState.Done(it.data.group) }
        }
    }
}

enum class HomeworkStatus {
    DONE, PENDING, OVERDUE
}