package plus.vplan.app.core.data.homework

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Optional
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import kotlin.uuid.Uuid

interface HomeworkRepository {
    fun getAll(): Flow<List<Homework>>
    fun getAllForProfile(profile: Profile): Flow<List<Homework>>
    fun getById(id: Int): Flow<Homework?>
    fun getByGroup(group: Group): Flow<List<Homework>>
    fun getByDate(date: LocalDate): Flow<List<Homework>>
    fun getByProfile(profileId: Uuid, date: LocalDate? = null): Flow<List<Homework>>
    fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>>

    suspend fun save(homework: Homework)
    suspend fun delete(homework: Homework)
    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
    suspend fun sync(vppId: VppId.Active? = null)
    suspend fun syncById(vppId: VppId.Active, homeworkId: Int, forceReload: Boolean = false): Boolean

    suspend fun getIdForNewLocalHomework(): Int
    suspend fun getIdForNewLocalHomeworkTask(): Int
    suspend fun getIdForNewLocalHomeworkFile(): Int

    suspend fun toggleTaskDone(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Boolean
    suspend fun addTask(homework: Homework, content: String, profile: Profile.StudentProfile): Response.Error?
    suspend fun updateTask(task: Homework.HomeworkTask, content: String, profile: Profile.StudentProfile)
    suspend fun deleteTask(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Response.Error?
    suspend fun deleteHomework(homework: Homework, profile: Profile.StudentProfile): Response.Error?

    suspend fun updateHomeworkMetadata(
        homework: Homework,
        dueTo: Optional<LocalDate> = Optional.Absent,
        subjectInstance: Optional<SubjectInstance?> = Optional.Absent,
        group: Optional<Group?> = Optional.Absent,
        isPublic: Optional<Boolean> = Optional.Absent,
        profile: Profile.StudentProfile
    )

    suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        groupId: Int?,
        subjectInstanceId: Int?,
        isPublic: Boolean,
        tasks: List<String>,
    ): Response<CreateHomeworkResponse>

    suspend fun linkHomeworkFile(vppId: VppId.Active?, homeworkId: Int, fileId: Int): Response<Unit>
    suspend fun unlinkHomeworkFile(vppId: VppId.Active?, homeworkId: Int, fileId: Int): Response<Unit>

    suspend fun clearCache()
    suspend fun dropIndexForProfile(profileId: Uuid)
    suspend fun createCacheForProfile(profileId: Uuid, homeworkIds: Collection<Int>)
}

data class CreateHomeworkResponse(
    val id: Int,
    val taskIds: Map<String, Int>
)
