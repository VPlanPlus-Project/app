package plus.vplan.app.domain.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.core.common.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.core.common.usecase.GetCurrentProfileUseCase
import plus.vplan.app.core.common.usecase.GetDayUseCase
import plus.vplan.app.core.data.populator.DayPopulator
import plus.vplan.app.domain.usecase.CheckEMailStructureUseCase
import plus.vplan.app.domain.usecase.OnNotificationGrantedUseCase
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase
import plus.vplan.app.feature.file.core.domain.usecase.DeleteFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.DownloadFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.GetFileThumbnailUseCase
import plus.vplan.app.feature.file.core.domain.usecase.OpenFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.RenameFileUseCase
import plus.vplan.app.feature.file.core.domain.usecase.UploadFileUseCase

val domainModule = module {
    singleOf(::GetCurrentDateTimeUseCase)
    singleOf(::GetCurrentProfileUseCase)
    singleOf(::SetCurrentProfileUseCase)
    singleOf(::GetDayUseCase)
    singleOf(::OnNotificationGrantedUseCase)
    singleOf(::CheckEMailStructureUseCase)
    singleOf(::UpdateFirebaseTokenUseCase)

    singleOf(::DayPopulator)

    // New file use cases
    singleOf(::UploadFileUseCase)
    singleOf(::DownloadFileUseCase)
    singleOf(::OpenFileUseCase)
    singleOf(::GetFileThumbnailUseCase)
    singleOf(::RenameFileUseCase)
    singleOf(::DeleteFileUseCase)
}
