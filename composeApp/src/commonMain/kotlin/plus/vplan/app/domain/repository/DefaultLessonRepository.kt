package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.SchoolApiAccess

interface DefaultLessonRepository {
    fun getByGroup(groupId: Int): Flow<List<DefaultLesson>>
    fun getBySchool(schoolId: Int, forceReload: Boolean): Flow<List<DefaultLesson>>
    fun getById(id: Int): Flow<CacheState<DefaultLesson>>
    fun getByIndiwareId(indiwareId: String): Flow<CacheState<DefaultLesson>>

    suspend fun download(schoolId: Int, schoolApiAccess: SchoolApiAccess): Response<List<DefaultLesson>>

    suspend fun upsert(defaultLesson: DefaultLesson): DefaultLesson
    suspend fun upsert(defaultLessons: List<DefaultLesson>)
    suspend fun deleteById(id: Int)
    suspend fun deleteById(ids: List<Int>)
}