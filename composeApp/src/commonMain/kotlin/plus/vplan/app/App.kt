package plus.vplan.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import plus.vplan.app.domain.source.AssessmentSource
import plus.vplan.app.domain.source.CourseSource
import plus.vplan.app.domain.source.DaySource
import plus.vplan.app.domain.source.DefaultLessonSource
import plus.vplan.app.domain.source.FileSource
import plus.vplan.app.domain.source.GroupSource
import plus.vplan.app.domain.source.HomeworkSource
import plus.vplan.app.domain.source.HomeworkTaskSource
import plus.vplan.app.domain.source.LessonTimeSource
import plus.vplan.app.domain.source.ProfileSource
import plus.vplan.app.domain.source.RoomSource
import plus.vplan.app.domain.source.SchoolSource
import plus.vplan.app.domain.source.SubstitutionPlanSource
import plus.vplan.app.domain.source.TeacherSource
import plus.vplan.app.domain.source.TimetableSource
import plus.vplan.app.domain.source.VppIdSource
import plus.vplan.app.domain.source.WeekSource
import plus.vplan.app.domain.source.schulverwalter.CollectionSource
import plus.vplan.app.domain.source.schulverwalter.GradeSource
import plus.vplan.app.domain.source.schulverwalter.IntervalSource
import plus.vplan.app.domain.source.schulverwalter.SubjectSource
import plus.vplan.app.domain.source.schulverwalter.YearSource
import plus.vplan.app.feature.host.ui.NavigationHost
import plus.vplan.app.ui.theme.AppTheme
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
        append("device_name", "mein telefon")
    }
).build().toString()

const val isDeveloperMode = true

object App {
    lateinit var vppIdSource: VppIdSource
    lateinit var homeworkSource: HomeworkSource
    lateinit var homeworkTaskSource: HomeworkTaskSource
    lateinit var profileSource: ProfileSource
    lateinit var groupSource: GroupSource
    lateinit var schoolSource: SchoolSource
    lateinit var defaultLessonSource: DefaultLessonSource
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

    lateinit var yearSource: YearSource
    lateinit var intervalSource: IntervalSource
    lateinit var collectionSource: CollectionSource
    lateinit var subjectSource: SubjectSource
    lateinit var schulverwalterTeacherSource: plus.vplan.app.domain.source.schulverwalter.TeacherSource
    lateinit var gradeSource: GradeSource
}

@Composable
@Preview
fun App(task: StartTask?) {
    AppTheme(dynamicColor = false) {
        KoinContext {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
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
    sealed class NavigateTo(profileId: Uuid?): StartTask(profileId) {
        class Calendar(profileId: Uuid?, val date: LocalDate): NavigateTo(profileId)
        class SchoolSettings(profileId: Uuid?, val openIndiwareSettingsSchoolId: Int? = null): NavigateTo(profileId)
    }

    sealed class Open(profileId: Uuid?): StartTask(profileId) {
        class Homework(profileId: Uuid?, val homeworkId: Int): Open(profileId)
        class Assessment(profileId: Uuid?, val assessmentId: Int): Open(profileId)
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
    }

    @Serializable
    data class StartTaskNavigateTo(
        @SerialName("screen") val screen: String,
        @SerialName("payload") val value: String
    ) {
        @Serializable
        data class StartTaskCalendar(
            @SerialName("date") val date: String
        )

        @Serializable
        data class SchoolSettings(
            @SerialName("open_indiware_settings_school_id") val openIndiwareSettingsSchoolId: Int? = null,
        )
    }
}