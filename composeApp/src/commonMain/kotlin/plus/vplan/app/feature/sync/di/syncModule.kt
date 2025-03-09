package plus.vplan.app.feature.sync.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.schulverwalter.SyncGradesUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase

val syncModule = module {
    singleOf(::UpdateWeeksUseCase)
    singleOf(::UpdateSubjectInstanceUseCase)
    singleOf(::UpdateLessonTimesUseCase)
    singleOf(::UpdateTimetableUseCase)
    singleOf(::UpdateSubstitutionPlanUseCase)
    singleOf(::UpdateHolidaysUseCase)

    singleOf(::UpdateHomeworkUseCase)
    singleOf(::UpdateAssessmentUseCase)
    singleOf(::SyncGradesUseCase)
    singleOf(::FullSyncUseCase)
}