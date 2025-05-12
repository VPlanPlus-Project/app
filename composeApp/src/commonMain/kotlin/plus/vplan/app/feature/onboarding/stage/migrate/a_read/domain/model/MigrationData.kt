package plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Migration(
    @SerialName("schools") val schools: List<SchoolMigration>,
    @SerialName("settings") val settings: SettingsMigration,
)

@Serializable
data class SchoolMigration(
    @SerialName("id") val id: Int,
    @SerialName("indiware_id") val indiwareId: String,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("profiles") val profiles: List<ProfileMigration>,
)

@Serializable
data class ProfileMigration(
    @SerialName("type") val type: String,
    @SerialName("entity_name") val entityName: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("default_lessons") val defaultLessons: List<DefaultLessonMigration>?,
    @SerialName("homework") val homework: List<HomeworkMigration>?,
    @SerialName("vpp_id") val vppIdToken: String? = null
)

@Serializable
data class DefaultLessonMigration(
    @SerialName("vp_id") val vpId: Int,
    @SerialName("enabled") val enabled: Boolean,
)

@Serializable
data class HomeworkMigration(
    @SerialName("vp_id") val vpId: Int?,
    @SerialName("date") val date: String,
    @SerialName("tasks") val tasks: List<HomeworkTaskMigration>,
)

@Serializable
data class HomeworkTaskMigration(
    @SerialName("task") val task: String,
    @SerialName("is_done") val isDone: Boolean,
)

@Serializable
data class SettingsMigration(
    @SerialName("protect_grades") val protectGrades: Boolean
)