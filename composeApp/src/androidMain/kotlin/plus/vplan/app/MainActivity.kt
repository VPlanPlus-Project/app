package plus.vplan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = this
        FileKit.init(this)
        enableEdgeToEdge()

        var task: StartTask? = null

        intent?.let {
            val action = it.action
            val data = it.data
            Logger.d { "Action: $action, Data: $data" }
            if (action == "android.intent.action.VIEW" && data.toString().startsWith("vpp://app/auth/")) {
                val token = data.toString().substringAfter("vpp://app/auth/")
                task = StartTask.VppIdLogin(token)
            }

            if (intent.hasExtra("onClickData")) {
                Logger.d { "Intent Task: ${intent.getStringExtra("onClickData")}" }
                val json = Json { ignoreUnknownKeys = true }
                val taskJson = json.decodeFromString<StartTaskJson>(intent.getStringExtra("onClickData")!!)
                when (taskJson.type) {
                    "navigate_to" -> {
                        val navigationJson = json.decodeFromString<StartTaskJson.StartTaskNavigateTo>(taskJson.value)
                        when (navigationJson.screen) {
                            "calendar" -> {
                                val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.StartTaskCalendar>(navigationJson.value)
                                task = StartTask.NavigateTo.Calendar(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, LocalDate.parse(payload.date))
                            }
                            "settings/school" -> {
                                val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.SchoolSettings>(navigationJson.value)
                                task = StartTask.NavigateTo.SchoolSettings(null, payload.openIndiwareSettingsSchoolId)
                            }
                        }
                    }
                    "open" -> {
                        val openJson = json.decodeFromString<StartTaskJson.StartTaskOpen>(taskJson.value)
                        when (openJson.type) {
                            "homework" -> {
                                val payload = json.decodeFromString<StartTaskJson.StartTaskOpen.Homework>(openJson.value)
                                task = StartTask.Open.Homework(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.homeworkId)
                            }
                            "assessment" -> {
                                val payload = json.decodeFromString<StartTaskJson.StartTaskOpen.Assessment>(openJson.value)
                                task = StartTask.Open.Assessment(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.assessmentId)
                            }
                        }
                    }
                }
            }
        }

        setContent { App(task) }
    }
}

lateinit var activity: MainActivity