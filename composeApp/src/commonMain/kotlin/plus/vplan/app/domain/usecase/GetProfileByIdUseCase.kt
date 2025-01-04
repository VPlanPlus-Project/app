package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import kotlin.uuid.Uuid

class GetProfileByIdUseCase {
    private val configuration = Profile.Fetch(
        studentProfile = Profile.StudentProfile.Fetch(
            group = Group.Fetch(school = School.Fetch())
        )
    )
    operator fun invoke(id: Uuid): Flow<Profile?> = flow {
        App.profileSource.getById(id.toHexString(), configuration)
            .filter { it is Cacheable.NotExisting || it is Cacheable.Loaded<Profile> }
            .collect {
                if (it is Cacheable.NotExisting) return@collect emit(null)
                if (it !is Cacheable.Loaded<Profile>) return@collect
                if (it.isConfigSatisfied(configuration, false)) return@collect emit(it.value)
            }
    }
}