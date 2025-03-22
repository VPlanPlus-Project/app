package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.School

interface RoomRepository: WebEntityRepository<Room> {
    fun getBySchool(schoolId: Int): Flow<List<Room>>
    suspend fun getBySchoolWithCaching(school: School, forceReload: Boolean): Response<Flow<List<Room>>>
}