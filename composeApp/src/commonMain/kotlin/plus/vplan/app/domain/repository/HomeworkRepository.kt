package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.homework.ui.components.File

interface HomeworkRepository {
    suspend fun upsert(homework: List<Homework>, tasks: List<Homework.HomeworkTask>, files: List<Homework.HomeworkFile>)
    suspend fun getByGroup(groupId: Int): Flow<List<Homework>>
    suspend fun getByGroup(authentication: SchoolApiAccess, groupId: Int, from: LocalDateTime? = null, to: LocalDate? = null): Response<List<HomeworkResponse>>

    fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>>

    fun getById(id: Int, forceReload: Boolean = false): Flow<CacheState<Homework>>
    fun getAll(): Flow<List<CacheState<Homework>>>

    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)

    suspend fun getIdForNewLocalHomework(): Int
    suspend fun getIdForNewLocalHomeworkTask(): Int
    suspend fun getIdForNewLocalHomeworkFile(): Int

    suspend fun toggleHomeworkTaskDone(task: Homework.HomeworkTask, profile: Profile.StudentProfile)
    suspend fun editHomeworkDefaultLesson(homework: Homework, defaultLesson: DefaultLesson?, group: Group?, profile: Profile.StudentProfile)
    suspend fun editHomeworkDueTo(homework: Homework, dueTo: LocalDate, profile: Profile.StudentProfile)
    suspend fun editHomeworkVisibility(homework: Homework.CloudHomework, isPublic: Boolean, profile: Profile.StudentProfile)

    suspend fun addTask(homework: Homework, task: String, profile: Profile.StudentProfile): Response.Error?
    suspend fun editHomeworkTask(task: Homework.HomeworkTask, newContent: String, profile: Profile.StudentProfile)

    suspend fun deleteHomework(homework: Homework, profile: Profile.StudentProfile): Response.Error?
    suspend fun deleteHomeworkTask(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Response.Error?

    /**
     * @return List of ids of the created homework
     */
    suspend fun download(
        schoolApiAccess: SchoolApiAccess,
        groupId: Int,
        defaultLessonIds: List<String>
    ): Response<List<Int>>

    suspend fun clearCache()

    suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        group: Group,
        defaultLesson: DefaultLesson?,
        isPublic: Boolean,
        tasks: List<String>,
    ): Response<CreateHomeworkResponse>

    suspend fun uploadHomeworkDocument(
        vppId: VppId.Active,
        homeworkId: Int,
        document: File
    ): Response<Int>
}

data class CreateHomeworkResponse(
    val id: Int,
    val taskIds: Map<String, Int>
)

data class HomeworkResponse(
    val id: Int,
    val createdBy: Int,
    val createdAt: Instant,
    val dueTo: Instant,
    val isPublic: Boolean,
    val group: Int?,
    val defaultLesson: String?,
    val tasks: List<HomeworkTaskResponse>
)

data class HomeworkTaskResponse(
    val id: Int,
    val content: String
)