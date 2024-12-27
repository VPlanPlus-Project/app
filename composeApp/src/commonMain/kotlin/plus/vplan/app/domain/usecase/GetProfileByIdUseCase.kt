package plus.vplan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class GetProfileByIdUseCase(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(id: Uuid): Flow<Profile?> {
        return profileRepository.getById(id)
    }
}