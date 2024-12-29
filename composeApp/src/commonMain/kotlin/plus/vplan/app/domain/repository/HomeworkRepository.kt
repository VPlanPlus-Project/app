package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.SchoolApiAccess

interface HomeworkRepository {
    suspend fun getByDefaultLesson(authentication: SchoolApiAccess, defaultLessonIds: List<String>, from: LocalDateTime? = null, to: LocalDate? = null): Response<List<HomeworkResponse>>
    suspend fun upsert(homework: List<Homework>)
    suspend fun getByGroup(groupId: Int): Flow<List<Homework>>

    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
}

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