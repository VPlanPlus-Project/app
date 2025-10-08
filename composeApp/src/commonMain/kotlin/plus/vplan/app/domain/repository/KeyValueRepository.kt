package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface KeyValueRepository {
    suspend fun set(key: String, value: String)
    suspend fun delete(key: String)
    fun get(key: String): Flow<String?>
    fun getBoolean(key: String): Flow<Boolean?> = get(key).map { it?.toBooleanStrictOrNull() }
    fun getBooleanOrDefault(key: String, default: Boolean): Flow<Boolean> = getBoolean(key).map { it ?: default }
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
    const val DEVELOPER_SETTINGS_DISABLE_AUTO_SYNC = "developer_settings_disable_auto_sync"

    /**
     * If true, the calendar view will always use the list view instead of the default calendar view.
     * This view is also used if there are not enough lesson times.
     */
    const val DS_FORCE_REDUCED_CALENDAR_VIEW = "ds_force_reduced_calendar_view"
    val forceReducedCalendarView = DeveloperFlag.Boolean(DS_FORCE_REDUCED_CALENDAR_VIEW, false)

    /**
     * If true, the timetable on the homescreen will always show all the lessons instead of featuring the current/next lesson
     * and hiding already passed lessons. This can be used to test the behavior when a school does not provide the necessary
     * lesson times to determine the current/next lesson.
     */
    const val DS_FORCE_STATIC_TIMETABLE_HOMESCREEN = "ds_force_static_timetable_homescreen"
    val forceStaticTimetableHomescreen = DeveloperFlag.Boolean(DS_FORCE_STATIC_TIMETABLE_HOMESCREEN, false)

    /**
     * A collection if all developer settings to easily show them in the developer settings UI.
     */
    val developerSettings = listOf(
        forceReducedCalendarView,
        forceStaticTimetableHomescreen
    )

    sealed class DeveloperFlag {
        abstract val key: String
        data class Boolean(override val key: String, val default: kotlin.Boolean) : DeveloperFlag()
    }
}