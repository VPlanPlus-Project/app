package plus.vplan.app.feature.schulverwalter.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.schulverwalter.CollectionRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.GradeRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.IntervalRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.SubjectRepositoryImpl
import plus.vplan.app.data.repository.schulverwalter.YearRepositoryImpl
import plus.vplan.app.domain.repository.schulverwalter.CollectionRepository
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository
import plus.vplan.app.domain.repository.schulverwalter.IntervalRepository
import plus.vplan.app.domain.repository.schulverwalter.SubjectRepository
import plus.vplan.app.domain.repository.schulverwalter.YearRepository

val schulverwalterModule = module {
    singleOf(::YearRepositoryImpl).bind<YearRepository>()
    singleOf(::IntervalRepositoryImpl).bind<IntervalRepository>()
    singleOf(::CollectionRepositoryImpl).bind<CollectionRepository>()
    singleOf(::SubjectRepositoryImpl).bind<SubjectRepository>()
    singleOf(::GradeRepositoryImpl).bind<GradeRepository>()
}