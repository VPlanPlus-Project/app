package plus.vplan.app.feature.home.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.home.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.home.domain.usecase.GetNewsUseCase
import plus.vplan.app.feature.home.domain.usecase.LoadNewsUseCase
import plus.vplan.app.feature.home.ui.HomeViewModel

val homeModule = module {
    singleOf(::GetCurrentProfileUseCase)
    singleOf(::LoadNewsUseCase)
    singleOf(::GetNewsUseCase)

    viewModelOf(::HomeViewModel)
}