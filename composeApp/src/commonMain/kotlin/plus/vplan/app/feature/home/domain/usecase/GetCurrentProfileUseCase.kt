package plus.vplan.app.feature.home.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository
) {

    private val configuration = Profile.Fetch(
        studentProfile = Profile.StudentProfile.Fetch(
            group = Group.Fetch(school = School.Fetch()),
            defaultLessons = DefaultLesson.Fetch(
                course = Course.Fetch()
            )
        )
    )

    operator fun invoke() = channelFlow {
        keyValueRepository.get(Keys.CURRENT_PROFILE).collectLatest { currentProfileId ->
            if (currentProfileId == null) return@collectLatest
            App.profileSource.getById(currentProfileId, configuration)
                .filterIsInstance<Cacheable.Loaded<Profile>>()
                .filter { it.isConfigSatisfied(configuration, false) }
                .map { it.value }
                .collectLatest {
                    send(it)
                }
        }
    }
}