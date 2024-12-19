package plus.vplan.app.feature.host.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.host.domain.usecase.HasProfileUseCase
import plus.vplan.app.feature.host.ui.NavigationHostViewModel

val hostModule = module {
    singleOf(::HasProfileUseCase)

    viewModelOf(::NavigationHostViewModel)
}