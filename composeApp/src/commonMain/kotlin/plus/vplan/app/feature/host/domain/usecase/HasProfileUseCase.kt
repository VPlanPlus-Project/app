package plus.vplan.app.feature.host.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.repository.ProfileRepository

class HasProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Boolean {
        return profileRepository.getAll().first().isNotEmpty()
    }
}