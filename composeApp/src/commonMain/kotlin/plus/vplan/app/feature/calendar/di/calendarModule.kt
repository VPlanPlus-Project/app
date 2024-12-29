package plus.vplan.app.feature.calendar.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.calendar.ui.CalendarViewModel

val calendarModule = module {
    viewModelOf(::CalendarViewModel)
}