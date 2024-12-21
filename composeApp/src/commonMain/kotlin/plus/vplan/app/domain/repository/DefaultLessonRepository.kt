package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.DefaultLesson

interface DefaultLessonRepository {
    fun getByGroup(groupId: Int): Flow<List<DefaultLesson>>
    fun getById(id: String): Flow<DefaultLesson?>

    suspend fun upsert(defaultLesson: DefaultLesson): Flow<DefaultLesson>
}