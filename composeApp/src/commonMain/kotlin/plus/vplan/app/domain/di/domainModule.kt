package plus.vplan.app.domain.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.domain.model.populated.AssessmentPopulator
import plus.vplan.app.domain.model.populated.DayPopulator
import plus.vplan.app.domain.model.populated.HomeworkPopulator
import plus.vplan.app.domain.model.populated.LessonPopulator
import plus.vplan.app.domain.model.populated.besteschule.CollectionPopulator
import plus.vplan.app.domain.model.populated.besteschule.GradesPopulator
import plus.vplan.app.domain.model.populated.besteschule.IntervalPopulator
import plus.vplan.app.domain.usecase.CheckEMailStructureUseCase
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.domain.usecase.OnNotificationGrantedUseCase
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase

val domainModule = module {
    singleOf(::GetCurrentDateTimeUseCase)
    singleOf(::SetCurrentProfileUseCase)
    singleOf(::GetDayUseCase)
    singleOf(::OnNotificationGrantedUseCase)
    singleOf(::CheckEMailStructureUseCase)
    singleOf(::UpdateFirebaseTokenUseCase)

    singleOf(::LessonPopulator)
    singleOf(::HomeworkPopulator)
    singleOf(::DayPopulator)
    singleOf(::AssessmentPopulator)
    singleOf(::IntervalPopulator)
    singleOf(::CollectionPopulator)
    singleOf(::GradesPopulator)
}