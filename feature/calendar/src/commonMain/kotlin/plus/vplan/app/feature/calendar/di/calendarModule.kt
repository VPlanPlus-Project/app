package plus.vplan.app.feature.calendar.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.calendar.page.domain.usecase.DownloadDayIfNecessaryUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.GetFirstLessonStartUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.GetHolidaysUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.GetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.page.domain.usecase.SetLastDisplayTypeUseCase
import plus.vplan.app.feature.calendar.page.ui.CalendarViewModel

val calendarModule = module {
    singleOf(::GetLastDisplayTypeUseCase)
    singleOf(::SetLastDisplayTypeUseCase)
    singleOf(::GetFirstLessonStartUseCase)
    singleOf(::DownloadDayIfNecessaryUseCase)
    singleOf(::GetHolidaysUseCase)

    viewModelOf(::CalendarViewModel)
}