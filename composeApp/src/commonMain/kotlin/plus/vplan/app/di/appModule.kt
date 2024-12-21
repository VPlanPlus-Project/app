package plus.vplan.app.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.CourseRepositoryImpl
import plus.vplan.app.data.repository.DefaultLessonRepositoryImpl
import plus.vplan.app.data.repository.GroupRepositoryImpl
import plus.vplan.app.data.repository.IndiwareRepositoryImpl
import plus.vplan.app.data.repository.KeyValueRepositoryImpl
import plus.vplan.app.data.repository.ProfileRepositoryImpl
import plus.vplan.app.data.repository.RoomRepositoryImpl
import plus.vplan.app.data.repository.SchoolRepositoryImpl
import plus.vplan.app.data.repository.TeacherRepositoryImpl
import plus.vplan.app.data.repository.WeekRepositoryImpl
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.feature.home.di.homeModule
import plus.vplan.app.feature.host.di.hostModule
import plus.vplan.app.feature.onboarding.di.onboardingModule
import plus.vplan.app.feature.sync.di.syncModule

expect fun platformModule(): Module

val appModule = module(createdAtStart = true) {
    single<HttpClient> {
        HttpClient {
            install(HttpTimeout) {
                socketTimeoutMillis = 5_000
                connectTimeoutMillis = 5_000
                requestTimeoutMillis = 5_000
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
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(platformModule())
        modules(appModule, hostModule, syncModule, onboardingModule, homeModule)
    }
}