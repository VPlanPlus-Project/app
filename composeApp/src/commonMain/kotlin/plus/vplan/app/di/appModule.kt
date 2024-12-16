package plus.vplan.app.di

import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.GroupRepositoryImpl
import plus.vplan.app.data.repository.IndiwareRepositoryImpl
import plus.vplan.app.data.repository.RoomRepositoryImpl
import plus.vplan.app.data.repository.SchoolRepositoryImpl
import plus.vplan.app.data.repository.TeacherRepositoryImpl
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.di.onboardingModule

expect fun platformModule(): Module

val appModule = module(createdAtStart = true) {
    single<HttpClient> {
        HttpClient()
    }

    singleOf(::SchoolRepositoryImpl).bind<SchoolRepository>()
    singleOf(::GroupRepositoryImpl).bind<GroupRepository>()
    singleOf(::TeacherRepositoryImpl).bind<TeacherRepository>()
    singleOf(::RoomRepositoryImpl).bind<RoomRepository>()
    singleOf(::IndiwareRepositoryImpl).bind<IndiwareRepository>()
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(platformModule())
        modules(appModule, onboardingModule)
    }
}