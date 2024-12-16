package plus.vplan.app.di

import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.IndiwareRepositoryImpl
import plus.vplan.app.data.repository.SchoolRepositoryImpl
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.onboarding.di.onboardingModule

expect fun platformModule(): Module

val appModule = module(createdAtStart = true) {
    single<HttpClient> {
        HttpClient()
    }

    singleOf(::SchoolRepositoryImpl).bind<SchoolRepository>()
    singleOf(::IndiwareRepositoryImpl).bind<IndiwareRepository>()
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        configuration?.invoke(this)
        modules(platformModule())
        modules(appModule, onboardingModule)
    }
}