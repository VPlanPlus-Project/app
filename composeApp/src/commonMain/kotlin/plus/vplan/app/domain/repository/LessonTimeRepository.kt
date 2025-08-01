package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.LessonTime
import kotlin.uuid.Uuid

interface LessonTimeRepository {
    fun getByGroup(groupId: Uuid): Flow<List<LessonTime>>
    fun getBySchool(schoolId: Uuid): Flow<List<LessonTime>>
    fun getById(id: String): Flow<LessonTime?>

    suspend fun upsert(lessonTime: LessonTime): Flow<LessonTime>
    suspend fun upsert(lessonTimes: List<LessonTime>)
    suspend fun deleteById(id: String)
    suspend fun deleteById(ids: List<String>)
}