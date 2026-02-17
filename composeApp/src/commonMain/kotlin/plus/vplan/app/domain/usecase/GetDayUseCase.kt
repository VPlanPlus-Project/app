package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.Day

class GetDayUseCase {
    operator fun invoke(profile: Profile, date: LocalDate): Flow<Day> {
        val schoolId = profile.school.id
        return App.daySource.getById("${schoolId}/$date", contextProfile = profile)
            .filterIsInstance<CacheState.Done<Day>>()
            .filter { Day.DayTags.HAS_LESSONS in it.data.tags }
            .map { it.data }
    }
}