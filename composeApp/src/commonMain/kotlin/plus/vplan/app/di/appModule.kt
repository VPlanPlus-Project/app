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
import kotlinx.coroutines.MainScope
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
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.data.besteschule.IntervalsRepositoryImpl
import plus.vplan.app.core.data.besteschule.YearsRepository
import plus.vplan.app.core.data.besteschule.YearsRepositoryImpl
import plus.vplan.app.core.database.di.databaseModule
import plus.vplan.app.data.repository.AssessmentRepositoryImpl
import plus.vplan.app.data.repository.CourseRepositoryImpl
import plus.vplan.app.data.repository.DayRepositoryImpl
import plus.vplan.app.data.repository.FcmRepositoryImpl
import plus.vplan.app.data.repository.FileRepositoryImpl
import plus.vplan.app.data.repository.GroupRepositoryImpl
import plus.vplan.app.data.repository.HomeworkRepositoryImpl
import plus.vplan.app.data.repository.KeyValueRepositoryImpl
import plus.vplan.app.data.repository.LessonTimeRepositoryImpl
import plus.vplan.app.data.repository.NewsRepositoryImpl
import plus.vplan.app.data.repository.ProfileRepositoryImpl
import plus.vplan.app.data.repository.RoomRepositoryImpl
import plus.vplan.app.data.repository.SchoolRepositoryImpl
import plus.vplan.app.data.repository.Stundenplan24RepositoryImpl
import plus.vplan.app.data.repository.SubjectInstanceRepositoryImpl
import plus.vplan.app.data.repository.SubstitutionPlanRepositoryImpl
import plus.vplan.app.data.repository.TeacherRepositoryImpl
import plus.vplan.app.data.repository.TimetableRepositoryImpl
import plus.vplan.app.data.repository.VppIdRepositoryImpl
import plus.vplan.app.data.repository.WeekRepositoryImpl
import plus.vplan.app.data.repository.besteschule.BesteSchuleApiRepositoryImpl
import plus.vplan.app.data.repository.besteschule.BesteSchuleCollectionsRepositoryImpl
import plus.vplan.app.data.repository.besteschule.BesteSchuleGradesRepositoryImpl
import plus.vplan.app.data.repository.besteschule.BesteSchuleSubjectsRepositoryImpl
import plus.vplan.app.data.repository.besteschule.BesteSchuleTeachersRepositoryImpl
import plus.vplan.app.data.service.ProfileServiceImpl
import plus.vplan.app.data.service.SchoolServiceImpl
import plus.vplan.app.data.source.network.GenericAuthenticationProvider
import plus.vplan.app.data.source.network.SchoolAuthenticationProvider
import plus.vplan.app.data.source.network.VppIdAuthenticationProvider
import plus.vplan.app.domain.di.domainModule
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.FcmRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.NewsRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleCollectionsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleTeachersRepository
import plus.vplan.app.domain.service.ProfileService
import plus.vplan.app.domain.service.SchoolService
import plus.vplan.app.domain.source.AssessmentSource
import plus.vplan.app.domain.source.CourseSource
import plus.vplan.app.domain.source.DaySource
import plus.vplan.app.domain.source.FileSource
import plus.vplan.app.domain.source.GroupSource
import plus.vplan.app.domain.source.HomeworkSource
import plus.vplan.app.domain.source.HomeworkTaskSource
import plus.vplan.app.domain.source.LessonTimeSource
import plus.vplan.app.domain.source.ProfileSource
import plus.vplan.app.domain.source.RoomSource
import plus.vplan.app.domain.source.SchoolSource
import plus.vplan.app.domain.source.SubjectInstanceSource
import plus.vplan.app.domain.source.SubstitutionPlanSource
import plus.vplan.app.domain.source.TeacherSource
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
import plus.vplan.app.network.besteschule.IntervalApi
import plus.vplan.app.network.besteschule.IntervalApiImpl
import plus.vplan.app.network.besteschule.YearApi
import plus.vplan.app.network.besteschule.YearApiImpl

expect val platformModule: Module

val appModule = module(createdAtStart = true) {
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
    singleOf(::YearsRepositoryImpl).bind<YearsRepository>()
    singleOf(::IntervalsRepositoryImpl).bind<IntervalsRepository>()

    singleOf(::SchoolAuthenticationProvider)
    singleOf(::VppIdAuthenticationProvider)
    singleOf(::GenericAuthenticationProvider)

    singleOf(::SchoolRepositoryImpl).bind<SchoolRepository>()
    singleOf(::GroupRepositoryImpl).bind<GroupRepository>()
    singleOf(::TeacherRepositoryImpl).bind<TeacherRepository>()
    singleOf(::RoomRepositoryImpl).bind<RoomRepository>()
    singleOf(::Stundenplan24RepositoryImpl).bind<Stundenplan24Repository>()
    singleOf(::CourseRepositoryImpl).bind<CourseRepository>()
    singleOf(::SubjectInstanceRepositoryImpl).bind<SubjectInstanceRepository>()
    singleOf(::ProfileRepositoryImpl).bind<ProfileRepository>()
    singleOf(::KeyValueRepositoryImpl).bind<KeyValueRepository>()
    singleOf(::WeekRepositoryImpl).bind<WeekRepository>()
    singleOf(::DayRepositoryImpl).bind<DayRepository>()
    singleOf(::LessonTimeRepositoryImpl).bind<LessonTimeRepository>()
    singleOf(::TimetableRepositoryImpl).bind<TimetableRepository>()
    singleOf(::SubstitutionPlanRepositoryImpl).bind<SubstitutionPlanRepository>()
    singleOf(::VppIdRepositoryImpl).bind<VppIdRepository>()
    singleOf(::HomeworkRepositoryImpl).bind<HomeworkRepository>()
    singleOf(::FileRepositoryImpl).bind<FileRepository>()
    singleOf(::AssessmentRepositoryImpl).bind<AssessmentRepository>()
    singleOf(::NewsRepositoryImpl).bind<NewsRepository>()
    singleOf(::FcmRepositoryImpl).bind<FcmRepository>()

    singleOf(::BesteSchuleApiRepositoryImpl) bind BesteSchuleApiRepository::class
    singleOf(::BesteSchuleSubjectsRepositoryImpl) bind BesteSchuleSubjectsRepository::class
    singleOf(::BesteSchuleCollectionsRepositoryImpl) bind BesteSchuleCollectionsRepository::class
    singleOf(::BesteSchuleTeachersRepositoryImpl) bind BesteSchuleTeachersRepository::class
    singleOf(::BesteSchuleGradesRepositoryImpl) bind BesteSchuleGradesRepository::class


    singleOf(::SchoolServiceImpl).bind<SchoolService>()
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

        App.homeworkSource = HomeworkSource(koin.get())
        App.homeworkTaskSource = HomeworkTaskSource(koin.get())
        App.profileSource = ProfileSource(koin.get())
        App.groupSource = GroupSource(koin.get())
        App.schoolSource = SchoolSource(koin.get())
        App.subjectInstanceSource = SubjectInstanceSource(koin.get())
        App.daySource = DaySource(koin.get(), koin.get(), koin.get(), koin.get(), koin.get(), koin.get())
        App.timetableSource = TimetableSource(koin.get())
        App.weekSource = WeekSource(koin.get())
        App.courseSource = CourseSource(koin.get())
        App.teacherSource = TeacherSource(koin.get())
        App.roomSource = RoomSource(koin.get())
        App.lessonTimeSource = LessonTimeSource(koin.get())
        App.substitutionPlanSource = SubstitutionPlanSource(koin.get())
        App.assessmentSource = AssessmentSource(koin.get())
        App.fileSource = FileSource(koin.get(), koin.get())
    }
}