package plus.vplan.app.feature.sync.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateDefaultLessonsUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateLessonTimesUseCase

val syncModule = module {
    singleOf(::UpdateWeeksUseCase)
    singleOf(::UpdateDefaultLessonsUseCase)
    singleOf(::UpdateLessonTimesUseCase)
}