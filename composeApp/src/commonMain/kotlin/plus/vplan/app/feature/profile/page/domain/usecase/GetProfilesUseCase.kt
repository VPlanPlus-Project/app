package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Profile

class GetProfilesUseCase {
    operator fun invoke() = App.profileSource.getAll()
            .map { flowEmission ->
                flowEmission
                    .filterIsInstance<CacheState.Done<Profile>>()
                    .map { it.data }
                    .groupBy { it.school }
                    .mapValues { profilesBySchool ->
                        profilesBySchool.value.sortedBy { it.profileType.ordinal.toString() + it.name }
                    }
            }.distinctUntilChanged()
}