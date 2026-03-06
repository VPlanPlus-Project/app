@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.core.model.Alias
import plus.vplan.app.feature.host.ui.NavigationHost
import plus.vplan.app.feature.settings.page.info.domain.usecase.getSystemInfo
import plus.vplan.app.ui.theme.AppTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val APP_ID = "4"
const val APP_REDIRECT_URI = "vpp://app/auth/"
val VPP_ID_AUTH_URL = URLBuilder(currentConfiguration.authUrl).apply {
    appendPathSegments("authorize")
    parameters.append("client_id", APP_ID)
    parameters.append("redirect_uri", APP_REDIRECT_URI)
    parameters.append("device_name", getSystemInfo().deviceName)
}.buildString()

@Composable
fun App(task: StartTask?) {
    AppTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                NavigationHost(task)
            }
        }
    }
}

sealed class StartTask(val profileId: Uuid? = null) {
    data class VppIdLogin(val token: String) : StartTask()
    data class StartSchulverwalterReconnect(val userId: Int): StartTask()
    data class SchulverwalterReconnectDone(val schulverwalterAccessToken: String, val vppId: Int) : StartTask()
    data class OpenUrl(val url: String): StartTask()
    sealed class NavigateTo(profileId: Uuid?): StartTask(profileId) {
        class Calendar(profileId: Uuid?, val date: LocalDate): NavigateTo(profileId)
        class SchoolSettings(profileId: Uuid?, val openSp24SettingsSchoolId: Alias? = null): NavigateTo(profileId)
        class Grades(profileId: Uuid?, val vppId: Int): NavigateTo(profileId)
    }

    sealed class Open(profileId: Uuid?): StartTask(profileId) {
        class Homework(profileId: Uuid?, val homeworkId: Int): Open(profileId)
        class Assessment(profileId: Uuid?, val assessmentId: Int): Open(profileId)
        class Grade(profileId: Uuid?, val gradeId: Int): Open(profileId)
    }
}

@Serializable
data class StartTaskJson(
    @SerialName("type") val type: String,
    @SerialName("profile_id") val profileId: String? = null,
    @SerialName("value") val value: String
) {
    @Serializable
    data class StartTaskOpen(
        @SerialName("type") val type: String,
        @SerialName("payload") val value: String
    ) {
        @Serializable
        data class Homework(
            @SerialName("homework_id") val homeworkId: Int
        )

        @Serializable
        data class SchulverwalterReauth(
            @SerialName("user_id") val userId: Int,
        )

        @Serializable
        data class Assessment(
            @SerialName("assessment_id") val assessmentId: Int
        )

        @Serializable
        data class Grade(
            @SerialName("grade_id") val gradeId: Int
        )
    }

    @Serializable
    data class StartTaskNavigateTo(
        @SerialName("screen") val screen: String,
        @SerialName("payload") val value: String?
    ) {
        @Serializable
        data class StartTaskCalendar(
            @SerialName("date") val date: String
        )

        @Serializable
        data class SchoolSettings(
            @SerialName("open_school_alias") val openIndiwareSettingsSchool: Alias? = null,
        )

        @Serializable
        data class Grades(
            @SerialName("vpp_id") val vppId: Int
        )
    }
}

fun getTaskFromNotificationString(data: String): StartTask? {
    val json = Json { ignoreUnknownKeys = true }
    val taskJson = json.decodeFromString<StartTaskJson>(data)
    when (taskJson.type) {
        "navigate_to" -> {
            val navigationJson = json.decodeFromString<StartTaskJson.StartTaskNavigateTo>(taskJson.value)
            when (navigationJson.screen) {
                "calendar" -> {
                    val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.StartTaskCalendar>(navigationJson.value!!)
                    return StartTask.NavigateTo.Calendar(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, LocalDate.parse(payload.date))
                }
                "settings/school" -> {
                    val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.SchoolSettings>(navigationJson.value!!)
                    return StartTask.NavigateTo.SchoolSettings(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.openIndiwareSettingsSchool)
                }
                "grades" -> {
                    val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.Grades>(navigationJson.value!!)
                    return StartTask.NavigateTo.Grades(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.vppId)
                }
            }
        }
        "open" -> {
            val openJson = json.decodeFromString<StartTaskJson.StartTaskOpen>(taskJson.value)
            when (openJson.type) {
                "homework" -> {
                    val payload = json.decodeFromString<StartTaskJson.StartTaskOpen.Homework>(openJson.value)
                    return StartTask.Open.Homework(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.homeworkId)
                }
                "assessment" -> {
                    val payload = json.decodeFromString<StartTaskJson.StartTaskOpen.Assessment>(openJson.value)
                    return StartTask.Open.Assessment(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.assessmentId)
                }
                "grade" -> {
                    val payload = json.decodeFromString<StartTaskJson.StartTaskOpen.Grade>(openJson.value)
                    return StartTask.Open.Grade(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.gradeId)
                }
                "schulverwalter-reauth" -> {
                    val payload = json.decodeFromString<StartTaskJson.StartTaskOpen.SchulverwalterReauth>(openJson.value)
                    return StartTask.StartSchulverwalterReconnect(payload.userId)
                }
            }
        }
        "url" -> {
            return StartTask.OpenUrl(taskJson.value)
        }
    }
    return null
}