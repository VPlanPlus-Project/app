package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.DefaultLesson

interface DefaultLessonRepository {
    fun getByGroup(groupId: Int): Flow<List<DefaultLesson>>
    fun getBySchool(schoolId: Int): Flow<List<DefaultLesson>>
    fun getById(id: String): Flow<CacheState<DefaultLesson>>

    suspend fun upsert(defaultLesson: DefaultLesson): DefaultLesson
    suspend fun upsert(defaultLessons: List<DefaultLesson>)
    suspend fun deleteById(id: String)
    suspend fun deleteById(ids: List<String>)
}