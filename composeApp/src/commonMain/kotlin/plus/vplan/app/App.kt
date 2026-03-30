package plus.vplan.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.application.StartTaskJson
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.feature.host.ui.NavigationHost
import kotlin.uuid.Uuid

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