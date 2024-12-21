package plus.vplan.app.feature.home.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.home.ui.HomeViewModel

val homeModule = module {
    viewModelOf(::HomeViewModel)
}