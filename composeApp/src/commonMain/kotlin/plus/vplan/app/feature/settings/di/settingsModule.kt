package plus.vplan.app.feature.settings.di

import org.koin.dsl.module
import plus.vplan.app.feature.settings.page.info.di.infoFeedbackModule
import plus.vplan.app.feature.settings.page.school.di.schoolSettingsModule
import plus.vplan.app.feature.settings.page.security.di.securitySettingsModule

val settingsModule = module {
    includes(schoolSettingsModule, securitySettingsModule, infoFeedbackModule)
}