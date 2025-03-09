package plus.vplan.app.feature.profile.settings.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.CheckIfVppIdIsStillConnectedUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.DeleteProfileUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.GetVppIdDevicesUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.IsLastProfileOfSchoolUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.LogoutVppIdDeviceUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.LogoutVppIdUseCase
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.RenameProfileUseCase
import plus.vplan.app.feature.profile.settings.page.main.ui.ProfileSettingsViewModel
import plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management.VppIdManagementViewModel
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.GetCourseConfigurationUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.domain.usecase.SetProfileDefaultLessonEnabledUseCase
import plus.vplan.app.feature.profile.settings.page.subject_instances.ui.components.ProfileSubjectInstanceViewModel

val profileSettingsModule = module {
    singleOf(::RenameProfileUseCase)
    singleOf(::CheckIfVppIdIsStillConnectedUseCase)
    singleOf(::LogoutVppIdUseCase)
    singleOf(::LogoutVppIdDeviceUseCase)
    singleOf(::GetVppIdDevicesUseCase)
    singleOf(::IsLastProfileOfSchoolUseCase)
    singleOf(::DeleteProfileUseCase)

    singleOf(::GetCourseConfigurationUseCase)
    singleOf(::SetProfileDefaultLessonEnabledUseCase)

    viewModelOf(::ProfileSettingsViewModel)
    viewModelOf(::VppIdManagementViewModel)
    viewModelOf(::ProfileSubjectInstanceViewModel)
}