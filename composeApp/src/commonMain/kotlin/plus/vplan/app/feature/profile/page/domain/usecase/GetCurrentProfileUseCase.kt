package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapLatest
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository
) {

    private val configuration = Profile.Fetch(
        studentProfile = Profile.StudentProfile.Fetch(
            defaultLessons = DefaultLesson.Fetch()
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Profile> = channelFlow {
        keyValueRepository.get(Keys.CURRENT_PROFILE).collectLatest { currentProfileId ->
            if (currentProfileId == null) return@collectLatest
            App.profileSource.getById(currentProfileId, configuration)
                .filterIsInstance<Cacheable.Loaded<Profile>>()
                .filter { it.isConfigSatisfied(configuration, false) }
                .mapLatest { it.value }
                .collectLatest { send(it) }
        }
    }
}