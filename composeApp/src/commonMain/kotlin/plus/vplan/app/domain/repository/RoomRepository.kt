package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.School

interface RoomRepository {
     fun getBySchool(schoolId: Int): Flow<List<Room>>
    suspend fun getBySchoolWithCaching(school: School): Response<Flow<List<Room>>>
    fun getById(id: Int): Flow<Room?>
    suspend fun getByIdWithCaching(id: Int, school: School): Response<Flow<Room?>>
}