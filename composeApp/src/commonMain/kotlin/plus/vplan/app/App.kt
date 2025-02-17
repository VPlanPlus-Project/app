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
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
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
import plus.vplan.app.feature.host.ui.NavigationHost
import plus.vplan.app.ui.theme.AppTheme

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
}

@Composable
@Preview
fun App(task: StartTask?) {
    AppTheme(dynamicColor = false) {
        KoinContext {
            App.vppIdSource = VppIdSource(koinInject())
            App.homeworkSource = HomeworkSource(koinInject())
            App.homeworkTaskSource = HomeworkTaskSource(koinInject())
            App.profileSource = ProfileSource(koinInject())
            App.groupSource = GroupSource(koinInject())
            App.schoolSource = SchoolSource(koinInject())
            App.defaultLessonSource = DefaultLessonSource(koinInject())
            App.daySource = DaySource(koinInject(), koinInject(), koinInject(), koinInject())
            App.timetableSource = TimetableSource(koinInject())
            App.weekSource = WeekSource(koinInject())
            App.courseSource = CourseSource(koinInject())
            App.teacherSource = TeacherSource(koinInject())
            App.roomSource = RoomSource(koinInject())
            App.lessonTimeSource = LessonTimeSource(koinInject())
            App.substitutionPlanSource = SubstitutionPlanSource(koinInject())
            App.assessmentSource = AssessmentSource(koinInject())
            App.fileSource = FileSource(koinInject(), koinInject())

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

sealed class StartTask {
    data class VppIdLogin(val token: String) : StartTask()
}