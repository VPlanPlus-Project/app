@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import plus.vplan.app.domain.source.AssessmentSource
import plus.vplan.app.domain.source.CourseSource
import plus.vplan.app.domain.source.DaySource
import plus.vplan.app.domain.source.FileSource
import plus.vplan.app.domain.source.GroupSource
import plus.vplan.app.domain.source.HomeworkSource
import plus.vplan.app.domain.source.HomeworkTaskSource
import plus.vplan.app.domain.source.LessonTimeSource
import plus.vplan.app.domain.source.NewsSource
import plus.vplan.app.domain.source.ProfileSource
import plus.vplan.app.domain.source.RoomSource
import plus.vplan.app.domain.source.SchoolSource
import plus.vplan.app.domain.source.SubjectInstanceSource
import plus.vplan.app.domain.source.SubstitutionPlanSource
import plus.vplan.app.domain.source.TeacherSource
import plus.vplan.app.domain.source.TimetableSource
import plus.vplan.app.domain.source.VppIdSource
import plus.vplan.app.domain.source.WeekSource
import plus.vplan.app.domain.source.schulverwalter.CollectionSource
import plus.vplan.app.domain.source.schulverwalter.FinalGradeSource
import plus.vplan.app.domain.source.schulverwalter.GradeSource
import plus.vplan.app.domain.source.schulverwalter.IntervalSource
import plus.vplan.app.domain.source.schulverwalter.SubjectSource
import plus.vplan.app.domain.source.schulverwalter.YearSource
import plus.vplan.app.feature.host.ui.NavigationHost
import plus.vplan.app.feature.settings.page.info.domain.usecase.getSystemInfo
import plus.vplan.app.ui.theme.AppTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Host(
    val protocol: URLProtocol = URLProtocol.HTTPS,
    val host: String,
    val port: Int = 443,
) {
    val url = "${protocol.name}://$host:$port"
}

val api = Host(
    protocol = URLProtocol.HTTPS,
    host = "vplan.plus",
    port = 443
)

val sp24Service = Host(
    protocol = URLProtocol.HTTPS,
    host = "sp24.microservices.vplan.plus",
    port = 443
)

val schulverwalterReauthService = Host(
    protocol = URLProtocol.HTTPS,
    host = "schulverwalter-reauth.microservices.vplan.plus/",
    port = 443
)

val auth = Host(
    protocol = URLProtocol.HTTPS,
    host = "auth.vplan.plus",
    port = 443
)

const val APP_ID = "4"
const val APP_SECRET = "crawling-mom-yesterday-jazz-populace-napkin"
const val APP_REDIRECT_URI = "vpp://app/auth/"
val VPP_ID_AUTH_URL = URLBuilder(
    protocol = auth.protocol,
    host = auth.host,
    port = auth.port,
    pathSegments = listOf("authorize"),
    parameters = Parameters.build {
        append("client_id", APP_ID)
        append("client_secret", APP_SECRET)
        append("redirect_uri", APP_REDIRECT_URI)
        append("device_name", getSystemInfo().let { "${it.manufacturer} ${it.deviceName} (${it.device})" })
    }
).build().toString()

const val isDeveloperMode = true
const val ENABLE_KTOR_LOGGING = false

object App {
    lateinit var vppIdSource: VppIdSource
    lateinit var homeworkSource: HomeworkSource
    lateinit var homeworkTaskSource: HomeworkTaskSource
    lateinit var profileSource: ProfileSource
    lateinit var groupSource: GroupSource
    lateinit var schoolSource: SchoolSource
    lateinit var subjectInstanceSource: SubjectInstanceSource
    lateinit var daySource: DaySource
    lateinit var timetableSource: TimetableSource
    lateinit var weekSource: WeekSource
    lateinit var courseSource: CourseSource
    lateinit var teacherSource: TeacherSource
    lateinit var roomSource: RoomSource
    lateinit var lessonTimeSource: LessonTimeSource
    lateinit var substitutionPlanSource: SubstitutionPlanSource
    lateinit var assessmentSource: AssessmentSource
    lateinit var fileSource: FileSource
    lateinit var newsSource: NewsSource

    lateinit var yearSource: YearSource
    lateinit var intervalSource: IntervalSource
    lateinit var collectionSource: CollectionSource
    lateinit var subjectSource: SubjectSource
    lateinit var schulverwalterTeacherSource: plus.vplan.app.domain.source.schulverwalter.TeacherSource
    lateinit var gradeSource: GradeSource
    lateinit var finalGradeSource: FinalGradeSource

    const val VERSION_CODE: Int = 1
    const val VERSION_NAME: String = "0.0.1-alpha" // remember to update build.gradle.kts
}

@Composable
@Preview
fun App(task: StartTask?) {
    AppTheme(dynamicColor = false) {
        KoinContext {
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
}

sealed class StartTask(val profileId: Uuid? = null) {
    data class VppIdLogin(val token: String) : StartTask()
    data class SchulverwalterReconnect(val schulverwalterAccessToken: String, val vppId: Int) : StartTask()
    data class OpenUrl(val url: String): StartTask()
    sealed class NavigateTo(profileId: Uuid?): StartTask(profileId) {
        class Calendar(profileId: Uuid?, val date: LocalDate): NavigateTo(profileId)
        class SchoolSettings(profileId: Uuid?, val openIndiwareSettingsSchoolId: Int? = null): NavigateTo(profileId)
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
            @SerialName("open_indiware_settings_school_id") val openIndiwareSettingsSchoolId: Int? = null,
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
                    return StartTask.NavigateTo.SchoolSettings(taskJson.profileId?.let { profileId -> Uuid.parse(profileId) }, payload.openIndiwareSettingsSchoolId)
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
            }
        }
        "url" -> {
            return StartTask.OpenUrl(taskJson.value)
        }
    }
    return null
}

enum class Platform {
    Android, iOS
}

expect fun getPlatform(): Platform
expect fun capture(event: String, properties: Map<String, Any>?)