package plus.vplan.app.feature.homework.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.ui.components.NewHomeworkViewModel
import plus.vplan.app.feature.homework.ui.components.detail.DetailViewModel

val homeworkModule = module {
    singleOf(::IsVppIdBannerAllowedUseCase)
    singleOf(::HideVppIdBannerUseCase)
    singleOf(::CreateHomeworkUseCase)
    singleOf(::ToggleTaskDoneUseCase)

    viewModelOf(::NewHomeworkViewModel)
    viewModelOf(::DetailViewModel)
}