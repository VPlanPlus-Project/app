package plus.vplan.app.domain.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase

val domainModule = module {
    singleOf(::GetCurrentProfileUseCase)
    singleOf(::SetCurrentProfileUseCase)
}