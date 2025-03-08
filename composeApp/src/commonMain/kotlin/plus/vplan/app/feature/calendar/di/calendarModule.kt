package plus.vplan.app.feature.calendar.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.calendar.domain.usecase.GetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.domain.usecase.SetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.ui.CalendarViewModel

val calendarModule = module {
    singleOf(::GetLastDisplayTypeUseCase)
    singleOf(::SetLastDisplayTypeUseCase)

    viewModelOf(::CalendarViewModel)
}