package plus.vplan.app.feature.search.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.search.ui.main.SearchViewModel

val searchModule = module {
    viewModelOf(::SearchViewModel)
}