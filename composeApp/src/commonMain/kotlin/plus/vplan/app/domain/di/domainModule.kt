package plus.vplan.app.domain.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.domain.model.populated.DayPopulator
import plus.vplan.app.domain.model.populated.besteschule.CollectionPopulator
import plus.vplan.app.domain.model.populated.besteschule.GradesPopulator
import plus.vplan.app.domain.model.populated.besteschule.IntervalPopulator
import plus.vplan.app.domain.usecase.CheckEMailStructureUseCase
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.domain.usecase.OnNotificationGrantedUseCase
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase
import plus.vplan.app.domain.usecase.file.DeleteFileUseCase
import plus.vplan.app.domain.usecase.file.DownloadFileUseCase
import plus.vplan.app.domain.usecase.file.GetFileThumbnailUseCase
import plus.vplan.app.domain.usecase.file.OpenFileUseCase
import plus.vplan.app.domain.usecase.file.RenameFileUseCase
import plus.vplan.app.domain.usecase.file.UploadFileUseCase

val domainModule = module {
    singleOf(::GetCurrentDateTimeUseCase)
    singleOf(::SetCurrentProfileUseCase)
    singleOf(::GetDayUseCase)
    singleOf(::OnNotificationGrantedUseCase)
    singleOf(::CheckEMailStructureUseCase)
    singleOf(::UpdateFirebaseTokenUseCase)

    singleOf(::DayPopulator)
    singleOf(::IntervalPopulator)
    singleOf(::CollectionPopulator)
    singleOf(::GradesPopulator)
    
    // New file use cases
    singleOf(::UploadFileUseCase)
    singleOf(::DownloadFileUseCase)
    singleOf(::OpenFileUseCase)
    singleOf(::GetFileThumbnailUseCase)
    singleOf(::RenameFileUseCase)
    singleOf(::DeleteFileUseCase)
}
