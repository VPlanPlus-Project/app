package plus.vplan.app.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile

class GetDayUseCase {
    operator fun invoke(profile: Profile, date: LocalDate): Flow<Day> {
        return App.daySource.getById("${profile.school.id}/$date", contextProfile = profile)
            .filterIsInstance<CacheState.Done<Day>>()
            .filter { Day.DayTags.HAS_LESSONS in it.data.tags }
            .map { it.data }
            .onEach { Logger.d { "Day: $it" } }
    }
}