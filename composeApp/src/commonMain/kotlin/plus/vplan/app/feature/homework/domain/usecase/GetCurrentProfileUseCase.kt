package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys

class GetCurrentProfileUseCase(
    private val keyValueRepository: KeyValueRepository
) {

    private val configuration = Profile.Fetch(
        studentProfile = Profile.StudentProfile.Fetch(
            group = Group.Fetch(),
            defaultLessons = DefaultLesson.Fetch(
                course = Course.Fetch(),
                teacher = Teacher.Fetch(),
                groups = Group.Fetch()
            )
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke() = keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().flatMapLatest { profileId ->
            App.profileSource.getById(profileId, configuration)
                .filterIsInstance<Cacheable.Loaded<Profile>>()
                .mapNotNull { it.value }
                .filter { it.isConfigSatisfied(configuration, true) }
    }
}