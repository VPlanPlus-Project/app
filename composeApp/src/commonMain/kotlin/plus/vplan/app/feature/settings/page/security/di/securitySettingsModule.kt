package plus.vplan.app.feature.settings.page.security.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.settings.page.security.domain.usecase.GetBiometricDeviceStateUseCase
import plus.vplan.app.feature.settings.page.security.domain.usecase.GetGradeProtectionLevelUseCase
import plus.vplan.app.feature.settings.page.security.domain.usecase.SetGradeProtectionLevelUseCase
import plus.vplan.app.feature.settings.page.security.ui.SecuritySettingsViewModel

val securitySettingsModule = module {
    singleOf(::GetGradeProtectionLevelUseCase)
    singleOf(::SetGradeProtectionLevelUseCase)
    singleOf(::GetBiometricDeviceStateUseCase)

    viewModelOf(::SecuritySettingsViewModel)
}