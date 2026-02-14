package plus.vplan.app.feature.news.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.news.domain.usecase.GetNewsUseCase
import plus.vplan.app.feature.news.domain.usecase.SetNewsAsReadUseCase
import plus.vplan.app.feature.news.ui.NewsViewModel

val newsModule = module {
    singleOf(::SetNewsAsReadUseCase)
    singleOf(::GetNewsUseCase)

    viewModelOf(::NewsViewModel)
}