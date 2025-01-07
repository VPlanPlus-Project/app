package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId

interface HomeworkRepository {
    suspend fun getByDefaultLesson(authentication: SchoolApiAccess, defaultLessonIds: List<String>, from: LocalDateTime? = null, to: LocalDate? = null): Response<List<HomeworkResponse>>
    suspend fun upsert(homework: List<Homework>)
    suspend fun getByGroup(groupId: Int): Flow<List<Homework>>
    suspend fun getByGroup(authentication: SchoolApiAccess, groupId: Int, from: LocalDateTime? = null, to: LocalDate? = null): Response<List<HomeworkResponse>>
    suspend fun getById(id: Int): Flow<Cacheable<Homework>>
    fun getAll(): Flow<List<Cacheable<Homework>>>

    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)

    suspend fun getIdForNewLocalHomework(): Int
    suspend fun getIdForNewLocalHomeworkTask(): Int

    suspend fun download(
        schoolApiAccess: SchoolApiAccess,
        groupId: Int,
        defaultLessonIds: List<String>
    ): Response.Error?

    suspend fun clearCache()

    suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        group: Group,
        defaultLesson: DefaultLesson?,
        isPublic: Boolean,
        tasks: List<String>,
    ): Response<CreateHomeworkResponse>
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