package plus.vplan.app.feature.schulverwalter.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.schulverwalter.CollectionRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.FinalGradeRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.GradeRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.IntervalRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.SchulverwalterRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.SubjectRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.TeacherRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.YearRepositoryImpl
import plus.vplan.app.domain.repository.schulverwalter.CollectionRepository
import plus.vplan.app.domain.repository.schulverwalter.FinalGradeRepository
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository
import plus.vplan.app.domain.repository.schulverwalter.IntervalRepository
import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterRepository
import plus.vplan.app.domain.repository.schulverwalter.SubjectRepository
import plus.vplan.app.domain.repository.schulverwalter.TeacherRepository
import plus.vplan.app.domain.repository.schulverwalter.YearRepository
import plus.vplan.app.feature.schulverwalter.domain.usecase.InitializeSchulverwalterReauthUseCase
import plus.vplan.app.feature.schulverwalter.domain.usecase.UpdateSchulverwalterAccessUseCase

val schulverwalterModule = module {
    singleOf(::YearRepositoryImpl).bind<YearRepository>()
    singleOf(::IntervalRepositoryImpl).bind<IntervalRepository>()
    singleOf(::CollectionRepositoryImpl).bind<CollectionRepository>()
    singleOf(::SubjectRepositoryImpl).bind<SubjectRepository>()
    singleOf(::TeacherRepositoryImpl).bind<TeacherRepository>()
    singleOf(::GradeRepositoryImpl).bind<GradeRepository>()
    singleOf(::FinalGradeRepositoryImpl).bind<FinalGradeRepository>()
    singleOf(::SchulverwalterRepositoryImpl).bind<SchulverwalterRepository>()

    singleOf(::InitializeSchulverwalterReauthUseCase)
    singleOf(::UpdateSchulverwalterAccessUseCase)
}