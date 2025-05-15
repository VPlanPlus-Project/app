package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface KeyValueRepository {
    suspend fun set(key: String, value: String)
    suspend fun delete(key: String)
    fun get(key: String): Flow<String?>
}

object Keys {
    const val CURRENT_PROFILE = "current_profile"

    const val GRADE_PROTECTION_LEVEL = "grade_protection_level"
    const val GRADES_LOCKED = "grades_locked"

    const val CALENDAR_DISPLAY_TYPE = "calendar_display_type"

    const val SHOW_HOMEWORK_VPP_ID_BANNER = "show_homework_vpp_id_banner"

    const val VPP_ID_LOGIN_LINK_TO_PROFILE = "VPP_ID_LOGIN_LiNK_TO_PROFILE"

    const val PREVIOUS_APP_VERSION = "previous_app_version"

    const val MIGRATION_FLAG_ASSESSMENTS_HOMEWORK_INDICES = "migration_flag_assessments_homework_indices"

    const val FIREBASE_TOKEN = "firebase_token"
    const val FIREBASE_TOKEN_UPLOAD_SUCCESS = "firebase_token_upload_success"

    const val DEVELOPER_SETTINGS_ACTIVE = "developer_settings_active"
}