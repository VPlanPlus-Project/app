package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.profile.ProfileRepository

class GetProfilesUseCase: KoinComponent {
    private val profileRepository by inject<ProfileRepository>()

    operator fun invoke() = profileRepository.getAll()
            .map { flowEmission ->
                flowEmission
                    .groupBy { it.school }
                    .mapValues { profilesBySchool ->
                        profilesBySchool.value.sortedBy { it.profileType.ordinal.toString() + it.name }
                    }
            }.distinctUntilChanged()
}