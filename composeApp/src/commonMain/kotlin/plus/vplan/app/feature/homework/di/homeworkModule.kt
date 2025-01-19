package plus.vplan.app.feature.homework.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.homework.domain.usecase.AddTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.DeleteTaskUseCase
import plus.vplan.app.feature.homework.domain.usecase.DownloadFileUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDefaultLessonUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkDueToUseCase
import plus.vplan.app.feature.homework.domain.usecase.EditHomeworkVisibilityUseCase
import plus.vplan.app.feature.homework.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.feature.homework.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.UpdateTaskUseCase
import plus.vplan.app.feature.homework.ui.components.NewHomeworkViewModel
import plus.vplan.app.feature.homework.ui.components.detail.DetailViewModel

val homeworkModule = module {
    singleOf(::IsVppIdBannerAllowedUseCase)
    singleOf(::HideVppIdBannerUseCase)
    singleOf(::CreateHomeworkUseCase)
    singleOf(::ToggleTaskDoneUseCase)
    singleOf(::UpdateHomeworkUseCase)
    singleOf(::EditHomeworkDefaultLessonUseCase)
    singleOf(::EditHomeworkDueToUseCase)
    singleOf(::EditHomeworkVisibilityUseCase)
    singleOf(::DeleteHomeworkUseCase)
    singleOf(::AddTaskUseCase)
    singleOf(::UpdateTaskUseCase)
    singleOf(::DeleteTaskUseCase)
    singleOf(::DownloadFileUseCase)

    viewModelOf(::NewHomeworkViewModel)
    viewModelOf(::DetailViewModel)
}