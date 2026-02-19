@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.data.repository.HomeworkDto
import plus.vplan.app.domain.model.populated.PopulatedHomework
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface HomeworkRepository: WebEntityRepository<Homework> {

    suspend fun upsert(homeworkEntity: HomeworkEntity)
    fun getByLocalId(id: Int): Flow<Homework?>
    fun getTaskByLocalId(id: Int): Flow<Homework.HomeworkTask?>

    fun getAll(): Flow<List<Homework>>

    fun getByGroup(group: Group): Flow<List<Homework>>

    fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>>

    fun getByDate(date: LocalDate): Flow<List<Homework>>
    fun getByProfile(profileId: Uuid, date: LocalDate? = null): Flow<List<Homework>>

    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)

    suspend fun getIdForNewLocalHomework(): Int
    suspend fun getIdForNewLocalHomeworkTask(): Int
    suspend fun getIdForNewLocalHomeworkFile(): Int

    suspend fun toggleHomeworkTaskDone(task: Homework.HomeworkTask, profile: Profile.StudentProfile)
    suspend fun editHomeworkSubjectInstance(homework: PopulatedHomework, subjectInstance: SubjectInstance?, group: Group?, profile: Profile.StudentProfile)
    suspend fun editHomeworkDueTo(homework: Homework, dueTo: LocalDate, profile: Profile.StudentProfile)
    suspend fun editHomeworkVisibility(homework: Homework.CloudHomework, isPublic: Boolean, profile: Profile.StudentProfile)

    suspend fun addTask(homework: Homework, task: String, profile: Profile.StudentProfile): Response.Error?
    suspend fun editHomeworkTask(task: Homework.HomeworkTask, newContent: String, profile: Profile.StudentProfile)

    suspend fun deleteHomework(homework: Homework, profile: Profile.StudentProfile): Response.Error?
    suspend fun deleteHomeworkTask(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Response.Error?

    suspend fun download(
        schoolApiAccess: VppSchoolAuthentication,
        groups: List<Alias>,
        subjectInstanceAliases: List<Alias>
    ): Response<List<HomeworkDto>>

    suspend fun clearCache()

    /**
     * @param groupId The vpp id of the group, null if bound to subject instance
     * @param subjectInstanceId The vpp id of the subject instance, null if bound to group
     */
    suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        groupId: Int?,
        subjectInstanceId: Int?,
        isPublic: Boolean,
        tasks: List<String>,
    ): Response<CreateHomeworkResponse>

    /**
     * Links a file to a homework.
     * @param vppId If provided, the file will be linked on the api as well.
     * @param homeworkId If the homework ID is greater than zero (meaning it is stored in the cloud), the [vppId] will be required.
     */
    suspend fun linkHomeworkFile(
        vppId: VppId.Active?,
        homeworkId: Int,
        fileId: Int
    ): Response<Unit>

    /**
     * Unlinks a file from a homework.
     * @param vppId If provided, the file will be unlinked on the api as well.
     * @param homeworkId If the homework ID is greater than zero (meaning it is stored in the cloud), the [vppId] will be required.
     */
    suspend fun unlinkHomeworkFile(
        vppId: VppId.Active?,
        homeworkId: Int,
        fileId: Int
    ): Response<Unit>

    suspend fun dropIndexForProfile(profileId: Uuid)
    suspend fun createCacheForProfile(profileId: Uuid, homeworkIds: Collection<Int>)
}

data class CreateHomeworkResponse(
    val id: Int,
    val taskIds: Map<String, Int>
)

data class HomeworkEntity(
    val id: Int,
    val createdAt: Instant,
    val dueTo: LocalDate,
    val tasks: List<TaskEntity>,
    val subjectInstanceId: Uuid?,
    val groupId: Uuid?,
    val cachedAt: Instant,
    val createdByProfileId: Uuid?,
    val createdByVppId: Int?,
    val isPublic: Boolean,
) {
    data class TaskEntity(
        val id: Int,
        val homeworkId: Int,
        val createdAt: Instant,
        val content: String,
        val cachedAt: Instant,
    )
}

data class HomeworkTaskDbDto(
    val id: Int,
    val homeworkId: Int,
    val content: String,
    val createdAt: Instant
)

data class HomeworkTaskDoneAccountDbDto(
    val taskId: Int,
    val doneBy: Int,
    val isDone: Boolean
)

data class HomeworkTaskDoneProfileDbDto(
    val taskId: Int,
    val profileId: Uuid,
    val isDone: Boolean
)