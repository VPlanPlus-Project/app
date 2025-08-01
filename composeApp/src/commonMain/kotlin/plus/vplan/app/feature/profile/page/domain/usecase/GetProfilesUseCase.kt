package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile

class GetProfilesUseCase {
    operator fun invoke() = App.profileSource.getAll()
            .map { flowEmission ->
                flowEmission
                    .filterIsInstance<CacheStateOld.Done<Profile>>()
                    .map { it.data }
                    .groupBy { it.getSchool().getFirstValue()!! }
                    .mapValues { profilesBySchool ->
                        profilesBySchool.value.sortedBy { it.profileType.ordinal.toString() + it.name }
                    }
            }.distinctUntilChanged()
}