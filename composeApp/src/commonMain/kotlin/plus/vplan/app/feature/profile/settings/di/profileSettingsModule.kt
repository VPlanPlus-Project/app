package plus.vplan.app.feature.profile.settings.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.settings.domain.usecase.CheckIfVppIdIsStillConnectedUseCase
import plus.vplan.app.feature.profile.settings.domain.usecase.GetVppIdDevicesUseCase
import plus.vplan.app.feature.profile.settings.domain.usecase.LogoutVppIdDeviceUseCase
import plus.vplan.app.feature.profile.settings.domain.usecase.LogoutVppIdUseCase
import plus.vplan.app.feature.profile.settings.domain.usecase.RenameProfileUseCase
import plus.vplan.app.feature.profile.settings.ui.ProfileSettingsViewModel
import plus.vplan.app.feature.profile.settings.ui.vpp_id_management.VppIdManagementViewModel

val profileSettingsModule = module {
    singleOf(::RenameProfileUseCase)
    singleOf(::CheckIfVppIdIsStillConnectedUseCase)
    singleOf(::LogoutVppIdUseCase)
    singleOf(::LogoutVppIdDeviceUseCase)
    singleOf(::GetVppIdDevicesUseCase)

    viewModelOf(::ProfileSettingsViewModel)
    viewModelOf(::VppIdManagementViewModel)
}