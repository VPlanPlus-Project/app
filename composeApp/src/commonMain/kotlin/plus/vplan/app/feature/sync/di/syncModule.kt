package plus.vplan.app.feature.sync.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.besteschule.BesteSchuleSyncCoordinatorImpl
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSyncCoordinator
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.besteschule.SyncGradesUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentsUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateNewsUseCase

val syncModule = module {
    singleOf(::UpdateWeeksUseCase)
    singleOf(::UpdateSubjectInstanceUseCase)
    singleOf(::UpdateLessonTimesUseCase)
    singleOf(::UpdateTimetableUseCase)
    singleOf(::UpdateSubstitutionPlanUseCase)
    singleOf(::UpdateHolidaysUseCase)
    singleOf(::UpdateNewsUseCase)

    singleOf(::UpdateHomeworkUseCase)
    singleOf(::UpdateAssessmentsUseCase)
    singleOf(::BesteSchuleSyncCoordinatorImpl).bind<BesteSchuleSyncCoordinator>()
    singleOf(::SyncGradesUseCase)
    singleOf(::FullSyncUseCase)
}