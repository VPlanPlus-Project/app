package plus.vplan.app.domain.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue
import kotlin.uuid.Uuid

sealed class Homework(
    val creator: AppEntity
) : Item {
    abstract val id: Int
    abstract val createdAt: Instant
    abstract val dueTo: LocalDate
    abstract val taskIds: List<Int>
    abstract val subjectInstanceId: Int?
    abstract val files: List<Int>
    override fun getEntityId(): String = this.id.toString()
    abstract val cachedAt: Instant

    val subjectInstance by lazy { this.subjectInstanceId?.let { App.subjectInstanceSource.getById(it) } }
    val tasks by lazy {
        if (taskIds.isEmpty()) return@lazy flowOf(emptyList())
        combine(taskIds.map { id -> App.homeworkTaskSource.getById(id).filterIsInstance<CacheState.Done<HomeworkTask>>().map { it.data } }) { it.toList() }
    }

    abstract val group: Flow<CacheState<Group>>?

    var taskItems: List<HomeworkTask>? = null
        private set

    var fileItems: List<File>? = null
        private set

    fun getTasksFlow() = combine(taskIds.map { App.homeworkTaskSource.getById(it).filterIsInstance<CacheState.Done<HomeworkTask>>() }) { it.toList().map { it.data }.also { taskItems = it } }
    fun getStatusFlow(profile: Profile.StudentProfile) = getTasksFlow().map { tasks ->
        if (tasks.all { it.isDone(profile) }) HomeworkStatus.DONE
        else if (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date > dueTo) HomeworkStatus.OVERDUE
        else HomeworkStatus.PENDING
    }

    fun getFilesFlow() = combine(files.map { App.fileSource.getById(it).filterIsInstance<CacheState.Done<File>>() }) { it.toList().map { it.data } }

    suspend fun getTaskItems(): List<HomeworkTask> {
        return taskItems ?: taskIds.mapNotNull { App.homeworkTaskSource.getSingleById(it) }.also { taskItems = it }
    }

    suspend fun getFileItems(): List<File> {
        if (files.isEmpty()) return emptyList()
        return fileItems ?: combine(files.map { App.fileSource.getById(it) }) { it.toList().mapNotNull { (it as? CacheState.Done<File>)?.data } }.first().also { fileItems = it }
    }

    data class HomeworkTask(
        val id: Int,
        val content: String,
        val doneByProfiles: List<Uuid>,
        val doneByVppIds: List<Int>,
        val homework: Int,
        val cachedAt: Instant
    ) : Item {
        override fun getEntityId(): String = this.id.toString()

        var homeworkItem: Homework? = null
            private set

        suspend fun getHomeworkItem(): Homework? {
            return homeworkItem ?: App.homeworkSource.getById(homework).getFirstValue().also { homeworkItem = it }
        }

        fun isDone(profile: Profile.StudentProfile) = (profile.id in doneByProfiles && profile.vppIdId == null) || profile.vppIdId in doneByVppIds
    }

    data class HomeworkFile(
        val id: Int,
        val name: String,
        val homework: Int,
        val size: Long,
    ) : Item {
        override fun getEntityId(): String = this.id.toString()
    }

    data class CloudHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: LocalDate,
        override val taskIds: List<Int>,
        override val subjectInstanceId: Int?,
        override val files: List<Int>,
        override val cachedAt: Instant,
        val isPublic: Boolean,
        val groupId: Int?,
        val createdBy: Int,
    ) : Homework(
        creator = AppEntity.VppId(createdBy)
    ) {
        override fun copyBase(createdAt: Instant, dueTo: LocalDate, tasks: List<Int>, subjectInstance: Int?): Homework {
            return this.copy(
                createdAt = createdAt,
                dueTo = dueTo,
                taskIds = tasks,
                subjectInstanceId = subjectInstance,
                groupId = groupId
            )
        }

        override val group: Flow<CacheState<Group>>? = groupId?.let { App.groupSource.getById(groupId) }

        var createdByItem: VppId? = null
            private set

        suspend fun getCreatedBy(): VppId {
            return createdByItem ?: createdBy.let { createdById ->
                App.vppIdSource.getSingleById(createdById)!!.also { createdByItem = it }
            }
        }
    }

    data class LocalHomework(
        override val id: Int,
        override val createdAt: Instant,
        override val dueTo: LocalDate,
        override val taskIds: List<Int>,
        override val subjectInstanceId: Int?,
        override val files: List<Int>,
        override val cachedAt: Instant,
        val createdByProfile: Uuid
    ) : Homework(
        creator = AppEntity.Profile(createdByProfile)
    ) {
        var createdByProfileItem: Profile.StudentProfile? = null
            private set

        suspend fun getCreatedByProfile(): Profile.StudentProfile {
            return createdByProfileItem ?: createdByProfile.let { createdByProfileId ->
                App.profileSource.getById(createdByProfileId).getFirstValue().let { it as Profile.StudentProfile }.also { createdByProfileItem = it }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override val group: Flow<CacheState<Group>> by lazy {
            App.profileSource.getById(createdByProfile)
                .filterIsInstance<CacheState.Done<Profile.StudentProfile>>()
                .map { it.data }
                .flatMapLatest { App.groupSource.getById(it.groupId) }
        }

        override fun copyBase(createdAt: Instant, dueTo: LocalDate, tasks: List<Int>, subjectInstance: Int?): Homework {
            return this.copy(
                createdAt = createdAt,
                dueTo = dueTo,
                taskIds = tasks,
                subjectInstanceId = subjectInstance
            )
        }
    }

    abstract fun copyBase(
        createdAt: Instant = this.createdAt,
        dueTo: LocalDate = this.dueTo,
        tasks: List<Int> = this.taskIds,
        subjectInstance: Int? = this.subjectInstanceId
    ): Homework
}

enum class HomeworkStatus {
    DONE, PENDING, OVERDUE
}