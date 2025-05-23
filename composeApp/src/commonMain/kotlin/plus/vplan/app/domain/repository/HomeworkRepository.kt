package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.ui.common.AttachedFile
import kotlin.uuid.Uuid

interface HomeworkRepository: WebEntityRepository<Homework> {
    suspend fun upsert(homework: List<Homework>, tasks: List<Homework.HomeworkTask>, files: List<Homework.HomeworkFile>)
    fun getByGroup(groupId: Int): Flow<List<Homework>>

    fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>>

    fun getAll(): Flow<List<CacheState<Homework>>>
    fun getByDate(date: LocalDate): Flow<List<Homework>>
    fun getByProfile(profileId: Uuid, date: LocalDate? = null): Flow<List<Homework>>

    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)

    suspend fun getIdForNewLocalHomework(): Int
    suspend fun getIdForNewLocalHomeworkTask(): Int
    suspend fun getIdForNewLocalHomeworkFile(): Int

    suspend fun toggleHomeworkTaskDone(task: Homework.HomeworkTask, profile: Profile.StudentProfile)
    suspend fun editHomeworkSubjectInstance(homework: Homework, subjectInstance: SubjectInstance?, group: Group?, profile: Profile.StudentProfile)
    suspend fun editHomeworkDueTo(homework: Homework, dueTo: LocalDate, profile: Profile.StudentProfile)
    suspend fun editHomeworkVisibility(homework: Homework.CloudHomework, isPublic: Boolean, profile: Profile.StudentProfile)

    suspend fun addTask(homework: Homework, task: String, profile: Profile.StudentProfile): Response.Error?
    suspend fun editHomeworkTask(task: Homework.HomeworkTask, newContent: String, profile: Profile.StudentProfile)

    suspend fun linkHomeworkFileLocally(homework: Homework, file: plus.vplan.app.domain.model.File)
    suspend fun unlinkHomeworkFileLocally(homework: Homework, fileId: Int)

    suspend fun deleteHomework(homework: Homework, profile: Profile.StudentProfile): Response.Error?
    suspend fun deleteHomeworkTask(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Response.Error?

    /**
     * @return List of ids of the created homework
     */
    suspend fun download(
        schoolApiAccess: SchoolApiAccess,
        groupId: Int,
        subjectInstanceIds: List<Int>
    ): Response<List<Int>>

    suspend fun clearCache()

    suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        group: Group,
        subjectInstance: SubjectInstance?,
        isPublic: Boolean,
        tasks: List<String>,
    ): Response<CreateHomeworkResponse>

    suspend fun uploadHomeworkDocument(
        vppId: VppId.Active,
        homeworkId: Int,
        document: AttachedFile
    ): Response<Int>

    suspend fun dropIndexForProfile(profileId: Uuid)
    suspend fun createCacheForProfile(profileId: Uuid, homeworkIds: Collection<Int>)
}

data class CreateHomeworkResponse(
    val id: Int,
    val taskIds: Map<String, Int>
)