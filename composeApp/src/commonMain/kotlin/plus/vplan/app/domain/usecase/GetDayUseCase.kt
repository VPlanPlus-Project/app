package plus.vplan.app.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile

class GetDayUseCase {
    suspend operator fun invoke(profile: Profile, date: LocalDate): Flow<Day> {
        val schoolId = profile.getSchool().getFirstValue()!!.id
        return App.daySource.getById("${schoolId}/$date", contextProfile = profile)
            .filterIsInstance<CacheStateOld.Done<Day>>()
            .filter { Day.DayTags.HAS_LESSONS in it.data.tags }
            .map { it.data }
            .onEach { Logger.d { "Day: $it" } }
    }
}