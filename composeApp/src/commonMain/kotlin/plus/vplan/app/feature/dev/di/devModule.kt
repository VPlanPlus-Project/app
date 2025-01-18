package plus.vplan.app.feature.dev.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.dev.ui.DevViewModel

val devModule = module {
    viewModelOf(::DevViewModel)
}