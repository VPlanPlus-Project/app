package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile

class GetDayUseCase {
    suspend operator fun invoke(profile: Profile, date: LocalDate): Flow<Day> {
        val schoolId = GetSchoolOfProfileUseCase().invoke(profile)
        return App.daySource.getById("${schoolId}/$date")
            .filterIsInstance<CacheState.Done<Day>>()
            .map { dayEmission ->
//                dayEmission.value.copy(
//                    timetable = dayEmission.value.timetable.filterProfile(profile),
//                    nextSchoolDay = (dayEmission.value.nextSchoolDay as Cacheable.Loaded).value.copy(
//                        timetable = dayEmission.value.nextSchoolDay.value.timetable.filterProfile(profile)
//                    ).let { Cacheable.Loaded(it) }
//                )
                dayEmission.data
            }
    }
}