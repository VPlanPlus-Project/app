package plus.vplan.app.feature.onboarding.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.feature.onboarding.data.repository.OnboardingRepositoryImpl
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.usecase.GetOnboardingStateUseCase
import plus.vplan.app.feature.onboarding.domain.usecase.InitialiseOnboardingWithSchoolIdUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.SearchForSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.SelectSp24SchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearchViewModel
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui.OnboardingIndiwareLoginViewModel
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataUseCase
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.ui.OnboardingIndiwareDataDownloadViewModel
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.SelectProfileUseCase
import plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.OnboardingSelectProfileViewModel
import plus.vplan.app.feature.onboarding.stage.e_permissions.ui.OnboardingPermissionViewModel
import plus.vplan.app.feature.onboarding.ui.OnboardingHostViewModel

val onboardingModule = module {

    singleOf(::OnboardingRepositoryImpl).bind<OnboardingRepository>()

    singleOf(::InitialiseOnboardingWithSchoolIdUseCase)
    singleOf(::SearchForSchoolUseCase)
    singleOf(::SelectSp24SchoolUseCase)

    singleOf(::CheckCredentialsUseCase)

    singleOf(::SetUpSchoolDataUseCase)
    singleOf(::SelectProfileUseCase)
    singleOf(::GetOnboardingStateUseCase)

    viewModelOf(::OnboardingHostViewModel)
    viewModelOf(::OnboardingSchoolSearchViewModel)
    viewModelOf(::OnboardingIndiwareLoginViewModel)
    viewModelOf(::OnboardingIndiwareDataDownloadViewModel)
    viewModelOf(::OnboardingSelectProfileViewModel)
    viewModelOf(::OnboardingPermissionViewModel)
}