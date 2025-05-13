package plus.vplan.app.feature.settings.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.settings.page.developer.di.developerSettingsModule
import plus.vplan.app.feature.settings.page.info.di.infoFeedbackModule
import plus.vplan.app.feature.settings.page.school.di.schoolSettingsModule
import plus.vplan.app.feature.settings.page.security.di.securitySettingsModule
import plus.vplan.app.feature.settings.ui.SettingsViewModel

val settingsModule = module {
    includes(
        schoolSettingsModule,
        securitySettingsModule,
        infoFeedbackModule,
        developerSettingsModule
    )

    viewModelOf(::SettingsViewModel)
}