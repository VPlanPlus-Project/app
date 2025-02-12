package plus.vplan.app.feature.search.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.search.domain.usecase.GetAssessmentsForProfileUseCase
import plus.vplan.app.feature.search.domain.usecase.GetHomeworkForProfileUseCase
import plus.vplan.app.feature.search.domain.usecase.SearchUseCase
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.GetRoomOccupationMapUseCase
import plus.vplan.app.feature.search.subfeature.room_search.ui.RoomSearchViewModel
import plus.vplan.app.feature.search.ui.main.SearchViewModel

val searchModule = module {
    singleOf(::SearchUseCase)
    singleOf(::GetHomeworkForProfileUseCase)
    singleOf(::GetAssessmentsForProfileUseCase)
    singleOf(::GetRoomOccupationMapUseCase)

    viewModelOf(::SearchViewModel)
    viewModelOf(::RoomSearchViewModel)
}