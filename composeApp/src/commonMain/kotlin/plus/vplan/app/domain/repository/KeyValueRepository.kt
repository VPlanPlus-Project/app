package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface KeyValueRepository {
    suspend fun set(key: String, value: String)
    suspend fun delete(key: String)
    fun get(key: String): Flow<String?>
}

object Keys {
    const val CURRENT_PROFILE = "current_profile"
    const val TIMETABLE_VERSION = "timetable_version"
}