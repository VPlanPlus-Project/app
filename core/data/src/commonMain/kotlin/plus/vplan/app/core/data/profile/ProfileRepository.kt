package plus.vplan.app.core.data.profile

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Profile
import kotlin.uuid.Uuid

interface ProfileRepository {
    fun getAll(): Flow<List<Profile>>
    fun getById(id: Uuid): Flow<Profile?>

    suspend fun save(profile: Profile)
    suspend fun delete(profile: Profile)
}