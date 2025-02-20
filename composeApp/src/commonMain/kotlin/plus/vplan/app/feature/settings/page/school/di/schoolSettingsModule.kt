package plus.vplan.app.feature.settings.page.school.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.settings.page.school.domain.usecase.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.settings.page.school.domain.usecase.GetSchoolsUseCase
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsViewModel

val schoolSettingsModule = module {
    singleOf(::GetSchoolsUseCase)
    singleOf(::CheckSp24CredentialsUseCase)

    viewModelOf(::SchoolSettingsViewModel)
}