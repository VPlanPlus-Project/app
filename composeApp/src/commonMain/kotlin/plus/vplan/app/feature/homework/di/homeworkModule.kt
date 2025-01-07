package plus.vplan.app.feature.homework.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.feature.homework.ui.components.NewHomeworkViewModel

val homeworkModule = module {
    singleOf(::IsVppIdBannerAllowedUseCase)
    singleOf(::HideVppIdBannerUseCase)
    singleOf(::CreateHomeworkUseCase)
    singleOf(::GetCurrentProfileUseCase)

    viewModelOf(::NewHomeworkViewModel)
}