package plus.vplan.app.feature.host.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.repository.ProfileRepository

class HasProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return profileRepository.getAll().map { it.isNotEmpty() }
    }
}