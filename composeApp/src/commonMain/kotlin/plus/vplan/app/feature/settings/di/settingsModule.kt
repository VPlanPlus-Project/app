package plus.vplan.app.feature.settings.di

import org.koin.dsl.module
import plus.vplan.app.feature.settings.page.school.di.schoolSettingsModule

val settingsModule = module {
    includes(schoolSettingsModule)
}