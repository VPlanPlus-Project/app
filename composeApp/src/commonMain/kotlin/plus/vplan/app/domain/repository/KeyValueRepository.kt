package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface KeyValueRepository {
    suspend fun set(key: String, value: String)
    suspend fun delete(key: String)
    fun get(key: String): Flow<String?>
}

object Keys {
    const val CURRENT_PROFILE = "current_profile"
    fun timetableVersion(schoolId: Int) = "timetable_version::$schoolId"
    fun substitutionPlanVersion(schoolId: Int) = "substitution_plan_version::$schoolId"

    const val SHOW_HOMEWORK_VPP_ID_BANNER = "show_homework_vpp_id_banner"
}