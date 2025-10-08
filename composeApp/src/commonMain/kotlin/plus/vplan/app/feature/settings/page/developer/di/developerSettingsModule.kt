package plus.vplan.app.feature.settings.page.developer.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.settings.page.developer.flags.DeveloperFlagsViewModel
import plus.vplan.app.feature.settings.page.developer.timetable_debug.TimetableDebugViewModel
import plus.vplan.app.feature.settings.page.developer.ui.DeveloperSettingsViewModel

val developerSettingsModule = module {
    viewModelOf(::DeveloperSettingsViewModel)
    viewModelOf(::TimetableDebugViewModel)
    viewModelOf(::DeveloperFlagsViewModel)
}