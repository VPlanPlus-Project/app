package plus.vplan.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.FileKit
import io.ktor.http.URLBuilder
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

class MainActivity : FragmentActivity() {

    private var task: StartTask? by mutableStateOf(null)
    private var canStart by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onNewIntent(intent)

        activity = this
        FileKit.init(this)
        enableEdgeToEdge()

        setContent {
            fragmentActivity = LocalActivity.current as FragmentActivity
            if (canStart) App(task)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        canStart = false

        intent.let {
            val action = it.action
            val data = it.data
            Logger.d { "Action: $action, Data: $data" }
            if (action == "android.intent.action.VIEW" && data.toString().startsWith("vpp://app/")) {
                val url = URLBuilder(data.toString())
                if (data.toString().startsWith("vpp://app/auth/")) {
                    val token = data.toString().substringAfter("vpp://app/auth/")
                    task = StartTask.VppIdLogin(token)
                } else if (data.toString().startsWith("vpp://app/schulverwalter-reconnect")) {
                    val token = url.pathSegments.last()
                    val vppId = url.parameters["user_id"]!!.toInt()
                    task = StartTask.SchulverwalterReconnect(token, vppId)
                }
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
                                val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.StartTaskCalendar>(navigationJson.value!!)
                                task = StartTask.NavigateTo.Calendar(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, LocalDate.parse(payload.date))
                            }
                            "settings/school" -> {
                                val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.SchoolSettings>(navigationJson.value!!)
                                task = StartTask.NavigateTo.SchoolSettings(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.openIndiwareSettingsSchoolId)
                            }
                            "grades" -> {
                                val payload = json.decodeFromString<StartTaskJson.StartTaskNavigateTo.Grades>(navigationJson.value!!)
                                task = StartTask.NavigateTo.Grades(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.vppId)
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
                            "grade" -> {
                                val payload = json.decodeFromString<StartTaskJson.StartTaskOpen.Grade>(openJson.value)
                                task = StartTask.Open.Grade(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.gradeId)
                            }
                        }
                    }
                    "url" -> {
                        task = StartTask.OpenUrl(taskJson.value)
                    }
                }
            }
        }

        canStart = true
    }
}

lateinit var activity: MainActivity
lateinit var fragmentActivity: FragmentActivity