package plus.vplan.app.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.App
import plus.vplan.app.AppBuildConfig
import plus.vplan.app.LOG_HTTP_REQUESTS
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.data.assessment.AssessmentRepositoryImpl
import plus.vplan.app.core.data.besteschule.BesteSchuleRepository
import plus.vplan.app.core.data.besteschule.BesteSchuleRepositoryImpl
import plus.vplan.app.core.data.besteschule.CollectionsRepository
import plus.vplan.app.core.data.besteschule.CollectionsRepositoryImpl
import plus.vplan.app.core.data.besteschule.GradesRepository
import plus.vplan.app.core.data.besteschule.GradesRepositoryImpl
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.data.besteschule.IntervalsRepositoryImpl
import plus.vplan.app.core.data.besteschule.SubjectsRepository
import plus.vplan.app.core.data.besteschule.SubjectsRepositoryImpl
import plus.vplan.app.core.data.besteschule.TeachersRepository
import plus.vplan.app.core.data.besteschule.TeachersRepositoryImpl
import plus.vplan.app.core.data.besteschule.YearsRepository
import plus.vplan.app.core.data.besteschule.YearsRepositoryImpl
import plus.vplan.app.core.data.course.CourseRepository
import plus.vplan.app.core.data.course.CourseRepositoryImpl
import plus.vplan.app.core.data.day.DayRepository
import plus.vplan.app.core.data.day.DayRepositoryImpl
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.group.GroupRepositoryImpl
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.data.holiday.HolidayRepositoryImpl
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.data.homework.HomeworkRepositoryImpl
import plus.vplan.app.core.data.lesson_times.LessonTimeRepository
import plus.vplan.app.core.data.lesson_times.LessonTimeRepositoryImpl
import plus.vplan.app.core.data.news.NewsRepository
import plus.vplan.app.core.data.news.NewsRepositoryImpl
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.profile.ProfileRepositoryImpl
import plus.vplan.app.core.data.room.RoomRepository
import plus.vplan.app.core.data.room.RoomRepositoryImpl
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.data.school.SchoolRepositoryImpl
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepositoryImpl
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.data.teacher.TeacherRepositoryImpl
import plus.vplan.app.core.data.timetable.TimetableRepository
import plus.vplan.app.core.data.week.WeekRepository
import plus.vplan.app.core.data.week.WeekRepositoryImpl
import plus.vplan.app.core.database.di.databaseModule
import plus.vplan.app.data.repository.FcmRepositoryImpl
import plus.vplan.app.data.repository.FileRepositoryImpl
import plus.vplan.app.data.repository.KeyValueRepositoryImpl
import plus.vplan.app.data.repository.Stundenplan24RepositoryImpl
import plus.vplan.app.data.repository.SubstitutionPlanRepositoryImpl
import plus.vplan.app.data.repository.TimetableRepositoryImpl
import plus.vplan.app.data.repository.VppIdRepositoryImpl
import plus.vplan.app.data.service.ProfileServiceImpl
import plus.vplan.app.domain.di.domainModule
import plus.vplan.app.domain.repository.FcmRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.service.ProfileService
import plus.vplan.app.domain.source.FileSource
import plus.vplan.app.domain.source.LessonTimeSource
import plus.vplan.app.domain.source.SubstitutionPlanSource
import plus.vplan.app.domain.source.TimetableSource
import plus.vplan.app.domain.source.WeekSource
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.di.assessmentModule
import plus.vplan.app.feature.calendar.di.calendarModule
import plus.vplan.app.feature.grades.di.gradeModule
import plus.vplan.app.feature.home.di.homeModule
import plus.vplan.app.feature.homework.di.homeworkModule
import plus.vplan.app.feature.host.di.hostModule
import plus.vplan.app.feature.main.di.mainModule
import plus.vplan.app.feature.news.di.newsModule
import plus.vplan.app.feature.onboarding.di.onboardingModule
import plus.vplan.app.feature.profile.di.profileModule
import plus.vplan.app.feature.profile.page.di.profilePageModule
import plus.vplan.app.feature.profile.settings.di.profileSettingsModule
import plus.vplan.app.feature.schulverwalter.di.schulverwalterModule
import plus.vplan.app.feature.search.di.searchModule
import plus.vplan.app.feature.settings.di.settingsModule
import plus.vplan.app.feature.sync.di.syncModule
import plus.vplan.app.feature.system.di.systemModule
import plus.vplan.app.feature.vpp_id.di.vppIdModule
import plus.vplan.app.network.besteschule.BesteSchuleApi
import plus.vplan.app.network.besteschule.BesteSchuleApiImpl
import plus.vplan.app.network.besteschule.GradesApi
import plus.vplan.app.network.besteschule.GradesApiImpl
import plus.vplan.app.network.besteschule.IntervalApi
import plus.vplan.app.network.besteschule.IntervalApiImpl
import plus.vplan.app.network.besteschule.YearApi
import plus.vplan.app.network.besteschule.YearApiImpl
import plus.vplan.app.network.vpp.GenericAuthenticationProvider
import plus.vplan.app.network.vpp.SchoolAuthenticationProvider
import plus.vplan.app.network.vpp.VppIdAuthenticationProvider
import plus.vplan.app.network.vpp.assessment.AssessmentApi
import plus.vplan.app.network.vpp.assessment.AssessmentApiImpl
import plus.vplan.app.network.vpp.group.GroupApi
import plus.vplan.app.network.vpp.group.GroupApiImpl
import plus.vplan.app.network.vpp.homework.HomeworkApi
import plus.vplan.app.network.vpp.homework.HomeworkApiImpl
import plus.vplan.app.network.vpp.news.NewsApi
import plus.vplan.app.network.vpp.news.NewsApiImpl
import plus.vplan.app.network.vpp.school.SchoolApi
import plus.vplan.app.network.vpp.school.SchoolApiImpl
import plus.vplan.app.network.vpp.subject_instance.SubjectInstanceApi
import plus.vplan.app.network.vpp.subject_instance.SubjectInstanceApiImpl
import kotlin.time.Clock
import kotlin.time.Instant

expect val platformModule: Module

val appModule = module(createdAtStart = true) {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<HttpClient> {
        val appLogger = co.touchlab.kermit.Logger.withTag("Ktor Client")
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    classDiscriminator = "type"
                    encodeDefaults = true
                })
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()

                retryOnException(2, retryOnTimeout = true)

                retryIf { request, response ->
                    val isRequestToBesteSchule = request.url.host == "beste.schule"

                    if (isRequestToBesteSchule && response.status == HttpStatusCode.TooManyRequests) {
                        appLogger.w { "Too many requests to beste.schule" }
                        val rateLimitReset = response.headers["x-ratelimit-reset"]?.toLongOrNull()
                            ?: return@retryIf false

                        val rateLimitResetTimestamp = Instant.fromEpochSeconds(rateLimitReset)
                        val now = Clock.System.now()
                        if (now > rateLimitResetTimestamp) return@retryIf false

                        delayMillis { (rateLimitResetTimestamp - now).inWholeMilliseconds }
                        return@retryIf true
                    }

                    val isResponseFromVPPServer = response.headers["X-Backend-Family"] == "vpp.ID"
                    val isResponseSuccess = response.status.isSuccess()
                    if (isResponseFromVPPServer && response.status == HttpStatusCode.InternalServerError) {
                        MainScope().launch {
                            appLogger.e { "Something went wrong at ${request.method} ${request.url}: 500\n${response.bodyAsText()}" }
                        }
                        return@retryIf false
                    }
                    return@retryIf isResponseFromVPPServer && response.status.value in 500..599 && !isResponseSuccess
                }
            }

            if (LOG_HTTP_REQUESTS) install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        appLogger.i { message }
                    }
                }
            }

            install(DefaultRequest) {
                header("X-App", "VPlanPlus")
                header("X-App-Version", AppBuildConfig.APP_VERSION_CODE)
            }
        }
    }

    singleOf(::YearApiImpl).bind<YearApi>()
    singleOf(::IntervalApiImpl).bind<IntervalApi>()
    singleOf(::GradesApiImpl).bind<GradesApi>()
    singleOf(::BesteSchuleApiImpl).bind<BesteSchuleApi>()

    singleOf(::YearsRepositoryImpl).bind<YearsRepository>()
    singleOf(::IntervalsRepositoryImpl).bind<IntervalsRepository>()
    singleOf(::TeachersRepositoryImpl).bind<TeachersRepository>()
    singleOf(::SubjectsRepositoryImpl).bind<SubjectsRepository>()
    singleOf(::CollectionsRepositoryImpl).bind<CollectionsRepository>()
    singleOf(::GradesRepositoryImpl).bind<GradesRepository>()
    singleOf(::BesteSchuleRepositoryImpl).bind<BesteSchuleRepository>()

    singleOf(::SchoolAuthenticationProvider)
    singleOf(::VppIdAuthenticationProvider)
    singleOf(::GenericAuthenticationProvider)

    singleOf(::SchoolApiImpl).bind<SchoolApi>()
    singleOf(::GroupApiImpl).bind<GroupApi>()
    singleOf(::SubjectInstanceApiImpl).bind<SubjectInstanceApi>()
    singleOf(::NewsApiImpl).bind<NewsApi>()
    singleOf(::HomeworkApiImpl).bind<HomeworkApi>()
    singleOf(::AssessmentApiImpl).bind<AssessmentApi>()

    singleOf(::SchoolRepositoryImpl).bind<SchoolRepository>()
    singleOf(::GroupRepositoryImpl).bind<GroupRepository>()
    singleOf(::TeacherRepositoryImpl).bind<TeacherRepository>()
    singleOf(::CourseRepositoryImpl).bind<CourseRepository>()
    singleOf(::SubjectInstanceRepositoryImpl).bind<SubjectInstanceRepository>()
    singleOf(::WeekRepositoryImpl).bind<WeekRepository>()
    singleOf(::NewsRepositoryImpl).bind<NewsRepository>()
    singleOf(::HolidayRepositoryImpl).bind<HolidayRepository>()
    singleOf(::DayRepositoryImpl).bind<DayRepository>()
    singleOf(::RoomRepositoryImpl).bind<RoomRepository>()
    singleOf(::HomeworkRepositoryImpl).bind<HomeworkRepository>()
    singleOf(::AssessmentRepositoryImpl).bind<AssessmentRepository>()

    singleOf(::Stundenplan24RepositoryImpl).bind<Stundenplan24Repository>()
    singleOf(::ProfileRepositoryImpl).bind<ProfileRepository>()
    singleOf(::KeyValueRepositoryImpl).bind<KeyValueRepository>()
    singleOf(::LessonTimeRepositoryImpl).bind<LessonTimeRepository>()
    singleOf(::TimetableRepositoryImpl).bind<TimetableRepository>()
    singleOf(::SubstitutionPlanRepositoryImpl).bind<SubstitutionPlanRepository>()
    singleOf(::VppIdRepositoryImpl).bind<VppIdRepository>()
    singleOf(::FileRepositoryImpl).bind<FileRepository>()
    singleOf(::FcmRepositoryImpl).bind<FcmRepository>()

    singleOf(::ProfileServiceImpl).bind<ProfileService>()

    singleOf(::GetCurrentProfileUseCase)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(platformModule, databaseModule)
        modules(domainModule)
        modules(
            appModule,
            systemModule,
            hostModule,
            mainModule,
            syncModule,
            onboardingModule,
            homeModule,
            calendarModule,
            homeworkModule,
            assessmentModule,
            searchModule,
            profilePageModule,
            profileSettingsModule,
            vppIdModule,
            settingsModule,
            schulverwalterModule,
            gradeModule,
            profileModule,
            newsModule
        )

        App.timetableSource = TimetableSource(koin.get())
        App.weekSource = WeekSource(koin.get())
        App.lessonTimeSource = LessonTimeSource(koin.get())
        App.substitutionPlanSource = SubstitutionPlanSource(koin.get())
        App.fileSource = FileSource(koin.get(), koin.get())
    }
}