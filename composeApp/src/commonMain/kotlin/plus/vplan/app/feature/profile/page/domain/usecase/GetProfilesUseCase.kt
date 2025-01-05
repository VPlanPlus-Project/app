package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School

class GetProfilesUseCase {
    private val configuration = Profile.Fetch(
        studentProfile = Profile.StudentProfile.Fetch(
            group = Group.Fetch(school = School.Fetch())
        )
    )

    operator fun invoke() = App.profileSource.getAll(
        configuration = configuration
    )
        .map { flowEmission ->
            flowEmission
                .filterIsInstance<Cacheable.Loaded<Profile>>()
                .filter { it.isConfigSatisfied(configuration, allowLoading = false) }
                .map { it.value }
                .groupBy { it.school }
                .mapValues { profilesBySchool ->
                    profilesBySchool.value.sortedBy { it.profileType.ordinal.toString() + it.customName }
                }
        }.distinctUntilChanged()
}