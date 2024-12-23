package plus.vplan.app.domain.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase

val domainModule = module {
    singleOf(::GetCurrentProfileUseCase)
}