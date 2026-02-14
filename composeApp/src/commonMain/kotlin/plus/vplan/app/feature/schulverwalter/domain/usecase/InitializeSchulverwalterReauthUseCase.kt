package plus.vplan.app.feature.schulverwalter.domain.usecase

import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class InitializeSchulverwalterReauthUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(vppId: VppId.Active): String? {
        return (vppIdRepository.getSchulverwalterReauthUrl(vppId) as? Response.Success)?.data
    }
}