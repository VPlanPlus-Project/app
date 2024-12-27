package plus.vplan.app.feature.profile.settings.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.settings.ui.ProfileSettingsViewModel

val profileSettingsModule = module {
    viewModelOf(::ProfileSettingsViewModel)
}