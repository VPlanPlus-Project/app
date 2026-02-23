package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade

interface GradesRepository {
    fun getById(id: Int, forceRefresh: Boolean = false): Flow<BesteSchuleGrade?>
    fun getAll(forceRefresh: Boolean = false): Flow<List<BesteSchuleGrade>>
    fun getAllForUser(schulverwalterUserId: Int, forceRefresh: Boolean = false): Flow<List<BesteSchuleGrade>>

    suspend fun save(grade: BesteSchuleGrade)
    suspend fun removeGradesForUser(userId: Int)
}