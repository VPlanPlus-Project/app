package plus.vplan.app.feature.dev

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val devModule = module {
    viewModelOf(::DevViewModel)
}