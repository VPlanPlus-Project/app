package plus.vplan.app.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.AssessmentRepositoryImpl
import plus.vplan.app.data.repository.CourseRepositoryImpl
import plus.vplan.app.data.repository.DayRepositoryImpl
import plus.vplan.app.data.repository.DefaultLessonRepositoryImpl
import plus.vplan.app.data.repository.FileRepositoryImpl
import plus.vplan.app.data.repository.GroupRepositoryImpl
import plus.vplan.app.data.repository.HomeworkRepositoryImpl
import plus.vplan.app.data.repository.IndiwareRepositoryImpl
import plus.vplan.app.data.repository.KeyValueRepositoryImpl
import plus.vplan.app.data.repository.LessonTimeRepositoryImpl
import plus.vplan.app.data.repository.ProfileRepositoryImpl
import plus.vplan.app.data.repository.RoomRepositoryImpl
import plus.vplan.app.data.repository.SchoolRepositoryImpl
import plus.vplan.app.data.repository.SubstitutionPlanRepositoryImpl
import plus.vplan.app.data.repository.TeacherRepositoryImpl
import plus.vplan.app.data.repository.TimetableRepositoryImpl
import plus.vplan.app.data.repository.VppIdRepositoryImpl
import plus.vplan.app.data.repository.WeekRepositoryImpl
import plus.vplan.app.domain.di.domainModule
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.assessment.di.assessmentModule
import plus.vplan.app.feature.calendar.di.calendarModule
import plus.vplan.app.feature.dev.di.devModule
import plus.vplan.app.feature.home.di.homeModule
import plus.vplan.app.feature.homework.di.homeworkModule
import plus.vplan.app.feature.host.di.hostModule
import plus.vplan.app.feature.onboarding.di.onboardingModule
import plus.vplan.app.feature.profile.page.di.profileModule
import plus.vplan.app.feature.profile.settings.di.profileSettingsModule
import plus.vplan.app.feature.search.di.searchModule
import plus.vplan.app.feature.sync.di.syncModule
import plus.vplan.app.feature.vpp_id.di.vppIdModule

expect fun platformModule(): Module

val appModule = module(createdAtStart = true) {
    single<HttpClient> {
        val appLogger = co.touchlab.kermit.Logger.withTag("Ktor Client")
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        appLogger.i { message }
                    }
                }
            }
        }
    }

    singleOf(::SchoolRepositoryImpl).bind<SchoolRepository>()
    singleOf(::GroupRepositoryImpl).bind<GroupRepository>()
    singleOf(::TeacherRepositoryImpl).bind<TeacherRepository>()
    singleOf(::RoomRepositoryImpl).bind<RoomRepository>()
    singleOf(::IndiwareRepositoryImpl).bind<IndiwareRepository>()
    singleOf(::CourseRepositoryImpl).bind<CourseRepository>()
    singleOf(::DefaultLessonRepositoryImpl).bind<DefaultLessonRepository>()
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

    singleOf(::GetCurrentProfileUseCase)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(platformModule())
        modules(domainModule)
        modules(
            appModule,
            hostModule,
            syncModule,
            onboardingModule,
            homeModule,
            calendarModule,
            homeworkModule,
            assessmentModule,
            searchModule,
            profileModule,
            profileSettingsModule,
            vppIdModule,
        )
        modules(devModule)
    }
}