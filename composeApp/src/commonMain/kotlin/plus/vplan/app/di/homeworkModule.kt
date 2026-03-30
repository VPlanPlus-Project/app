package plus.vplan.app.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.core.common.domain.usecase.HideVppIdBannerUseCase
import plus.vplan.app.core.common.domain.usecase.IsVppIdBannerAllowedUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.AddTaskUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.DeleteHomeworkUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.DeleteTaskUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.EditHomeworkDueToUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.EditHomeworkSubjectInstanceUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.EditHomeworkVisibilityUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.UpdateHomeworkUseCase
import plus.vplan.app.feature.homework.core.domain.usecase.UpdateTaskUseCase
import plus.vplan.app.feature.homework.create.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.feature.homework.create.ui.NewHomeworkViewModel
import plus.vplan.app.feature.homework.detail.ui.HomeworkDetailViewModel

val homeworkModule = module {
    singleOf(::IsVppIdBannerAllowedUseCase)
    singleOf(::HideVppIdBannerUseCase)
    singleOf(::CreateHomeworkUseCase)
    singleOf(::ToggleTaskDoneUseCase)
    singleOf(::UpdateHomeworkUseCase)
    singleOf(::EditHomeworkSubjectInstanceUseCase)
    singleOf(::EditHomeworkDueToUseCase)
    singleOf(::EditHomeworkVisibilityUseCase)
    singleOf(::DeleteHomeworkUseCase)
    singleOf(::AddTaskUseCase)
    singleOf(::UpdateTaskUseCase)
    singleOf(::DeleteTaskUseCase)

    viewModelOf(::NewHomeworkViewModel)
    viewModelOf(::HomeworkDetailViewModel)
}