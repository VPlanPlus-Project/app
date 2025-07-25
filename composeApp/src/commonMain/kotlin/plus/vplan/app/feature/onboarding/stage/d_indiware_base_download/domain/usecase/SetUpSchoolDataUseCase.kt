@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import plus.vplan.app.appApi
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.Holiday
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateWeeksUseCase
import plus.vplan.lib.sp24.source.Authentication
import kotlin.time.ExperimentalTime

class SetUpSchoolDataUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val indiwareRepository: IndiwareRepository,
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val dayRepository: DayRepository,
    private val courseRepository: CourseRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val weekRepository: WeekRepository,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val httpClient: HttpClient
) {
    operator fun invoke(): Flow<SetUpSchoolDataResult> = channelFlow {
        val result = SetUpSchoolDataStep.entries.associateWith { SetUpSchoolDataState.NOT_STARTED }.toMutableMap()
        val trySendResult: suspend () -> Unit = { this@channelFlow.trySend(SetUpSchoolDataResult.Loading(result.toMap())) }
        trySend(SetUpSchoolDataResult.Loading(result.toMap()))
        val prefix = "Onboarding/${this::class.simpleName}"
        try {
            val sp24Id = onboardingRepository.getSp24OnboardingSchool().first()?.sp24Id?.toString()
                ?: run {
                    trySend(SetUpSchoolDataResult.Error("$prefix sp24Id is null"))
                    return@channelFlow
                }
            val username = onboardingRepository.getSp24Credentials()?.username
                ?: run {
                    trySend(SetUpSchoolDataResult.Error("$prefix username is null"))
                    return@channelFlow
                }
            val password = onboardingRepository.getSp24Credentials()?.password
                ?: run {
                    trySend(SetUpSchoolDataResult.Error("$prefix password is null"))
                    return@channelFlow
                }

            val sp24Authentication = Authentication(sp24Id, username, password)
            val client = indiwareRepository.getSp24Client(authentication = sp24Authentication, true)

            result[SetUpSchoolDataStep.DOWNLOAD_BASE_DATA] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val response = httpClient.post {
                url(URLBuilder(appApi).apply {
                    appendPathSegments("onboarding", "v1", "sp24", "lookup")
                }.build())

                contentType(ContentType.Application.Json)
                setBody(LookupSp24ConnectionRequest(
                    sp24Id = sp24Id.toInt(),
                    username = username,
                    password = password
                ))
            }
            require(response.status == HttpStatusCode.OK) { "Failed to lookup sp24 id on vpp server: ${response.status} - ${response.bodyAsText()}" }

            val responseBody = response.body<ResponseDataWrapper<Sp24LookupResponse>>().data
            val schoolId = if (responseBody !is Sp24LookupSchoolIdResponse) {
                require(responseBody is Sp24LookupJobIdResponse)
                while (true) {
                    val response = httpClient.get {
                        url(URLBuilder(appApi).apply {
                            appendPathSegments("onboarding", "v1", "sp24", "job-status")
                            parameters.append("job_id", responseBody.jobId.toString())
                        }.build())
                    }

                    val jobStatus = response.body<ResponseDataWrapper<Sp24InitVppJobStatus>>().data
                    if (jobStatus == Sp24InitVppJobStatus.Finished) break
                    delay(5000)
                    println("waiting 5000ms for next job check")
                }
                val response = httpClient.post {
                    url(URLBuilder(appApi).apply {
                        appendPathSegments("onboarding", "v1", "sp24", "lookup")
                    }.build())

                    contentType(ContentType.Application.Json)
                    setBody(LookupSp24ConnectionRequest(
                        sp24Id = sp24Id.toInt(),
                        username = username,
                        password = password
                    ))
                }

                response.body<ResponseDataWrapper<Sp24LookupSchoolIdResponse>>().data.schoolId
            } else {
                responseBody.schoolId
            }

            val baseData = indiwareRepository.getBaseData(
                Authentication(
                    indiwareSchoolId = sp24Id,
                    username = username,
                    password = password
                )
            )
            if (baseData !is Response.Success) {
                trySend(SetUpSchoolDataResult.Error("$prefix baseData is not successful: $baseData"))
                return@channelFlow
            }
            result[SetUpSchoolDataStep.DOWNLOAD_BASE_DATA] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_SCHOOL_INFORMATION] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val schoolFlow = (schoolRepository.getById(schoolId, false))
            schoolFlow.takeWhile { it is CacheState.Loading }.collect()
            val school = schoolFlow.first().let {
                if (it !is CacheState.Done) {
                    trySend(SetUpSchoolDataResult.Error("$prefix school-Lookup was not successful: $it"))
                    return@channelFlow
                }
                schoolRepository.setSp24Info(
                    school = it.data,
                    sp24Id = sp24Id.toInt(),
                    username = username,
                    password = password,
                    daysPerWeek = baseData.data.daysPerWeek,
                    studentsHaveFullAccess = baseData.data.studentsHaveFullAccess,
                    downloadMode = baseData.data.downloadMode
                )
                onboardingRepository.setSchoolId(it.data.id)
                schoolRepository.getById(it.data.id, false).onEach { Logger.d { it.toString() } }.getFirstValue()!!
            } as School.IndiwareSchool

            result[SetUpSchoolDataStep.GET_SCHOOL_INFORMATION] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_HOLIDAYS] = SetUpSchoolDataState.IN_PROGRESS
            dayRepository.upsert(baseData.data.holidays.map { Holiday(it, school.id) })
            result[SetUpSchoolDataStep.GET_HOLIDAYS] = SetUpSchoolDataState.DONE

            result[SetUpSchoolDataStep.GET_GROUPS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            groupRepository.getBySchoolWithCaching(school).let {
                (it as? Response.Success)?.data?.first() ?: run {
                    trySend(SetUpSchoolDataResult.Error("$prefix groups-Lookup was not successful: $it"))
                    return@channelFlow
                }
            }

            result[SetUpSchoolDataStep.GET_GROUPS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_TEACHERS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val teachers = teacherRepository.getBySchoolWithCaching(school, forceReload = true).let {
                (it as? Response.Success)?.data?.first() ?: run {
                    trySend(SetUpSchoolDataResult.Error("$prefix teachers-Lookup was not successful: $it"))
                    return@channelFlow
                }
            }

            result[SetUpSchoolDataStep.GET_TEACHERS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_ROOMS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            roomRepository.getBySchoolWithCaching(school, forceReload = true).let {
                (it as? Response.Success)?.data?.first() ?: run {
                    trySend(SetUpSchoolDataResult.Error("$prefix rooms-Lookup was not successful: $it"))
                    return@channelFlow
                }
            }

            result[SetUpSchoolDataStep.GET_ROOMS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_WEEKS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            require(updateWeeksUseCase(school, client) == null) { "Couldn't update weeks" }

            result[SetUpSchoolDataStep.GET_WEEKS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_LESSON_TIMES] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            require(updateLessonTimesUseCase(school) == null) { "Couldn't update lesson times" }

            result[SetUpSchoolDataStep.GET_LESSON_TIMES] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.SET_UP_DATA] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            courseRepository.getBySchool(school.id, true).first()
            baseData.data.classes
                .flatMap { baseDataClass -> baseDataClass.subjectInstances.mapNotNull { it.course }.map { Course.fromIndiware(sp24Id, it.name, teachers.firstOrNull { t -> t.name == it.teacher }) } }
                .distinct()
                .onEach { courseRepository.getByIndiwareId(it).getFirstValue() }

            subjectInstanceRepository.download(school.id, school.getSchoolApiAccess())

            subjectInstanceRepository.getBySchool(school.id, true).first()
            baseData.data.classes
                .flatMap { baseDataClass -> baseDataClass.subjectInstances.map { it.subjectInstanceNumber } }
                .distinct()
                .map { subjectInstanceRepository.getByIndiwareId(it).getFirstValue() }

            result[SetUpSchoolDataStep.SET_UP_DATA] = SetUpSchoolDataState.DONE
            return@channelFlow trySendResult()
        } catch (e: Exception) {
            e.printStackTrace()
            trySend(SetUpSchoolDataResult.Error(e.message ?: "Unknown error"))
            return@channelFlow
        }
    }
}

sealed class SetUpSchoolDataResult {
    data class Loading(val data: Map<SetUpSchoolDataStep, SetUpSchoolDataState> = SetUpSchoolDataStep.entries.associateWith { SetUpSchoolDataState.NOT_STARTED }) : SetUpSchoolDataResult()
    data class Error(val message: String) : SetUpSchoolDataResult() {
        init {
            Logger.e("SetUpSchoolDataResult.Error") { message }
        }
    }
}

enum class SetUpSchoolDataStep {
    DOWNLOAD_BASE_DATA,
    GET_SCHOOL_INFORMATION,
    GET_HOLIDAYS,
    GET_GROUPS,
    GET_TEACHERS,
    GET_ROOMS,
    GET_WEEKS,
    GET_LESSON_TIMES,
    SET_UP_DATA
}

enum class SetUpSchoolDataState {
    NOT_STARTED,
    IN_PROGRESS,
    DONE
}

@Serializable
data class LookupSp24ConnectionRequest(
    @SerialName("sp24_id") val sp24Id: Int,
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

@Polymorphic
@Serializable
sealed interface Sp24LookupResponse

@Serializable
@SerialName("job_id")
data class Sp24LookupJobIdResponse(
    @SerialName("job_id") val jobId: Int
) : Sp24LookupResponse

@Serializable
@SerialName("school_id")
data class Sp24LookupSchoolIdResponse(
    @SerialName("school_id") val schoolId: Int
) : Sp24LookupResponse

fun SerializersModuleBuilder.onboardingSp24VppLookupResponseModule() = polymorphic(Sp24LookupResponse::class) {
    subclass(Sp24LookupJobIdResponse::class)
    subclass(Sp24LookupSchoolIdResponse::class)
}

@Serializable
enum class Sp24InitVppJobStatus {
    @SerialName("running") Running,
    @SerialName("finished") Finished
}