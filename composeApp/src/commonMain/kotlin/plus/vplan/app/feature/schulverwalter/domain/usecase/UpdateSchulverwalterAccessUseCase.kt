package plus.vplan.app.feature.schulverwalter.domain.usecase

import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterRepository

class UpdateSchulverwalterAccessUseCase(
    private val schulverwalterRepository: SchulverwalterRepository
) {
    suspend operator fun invoke(vppIdId: Int, accessToken: String) {
        schulverwalterRepository.setSchulverwalterAccessTokenForUser(vppIdId, accessToken)
    }
}