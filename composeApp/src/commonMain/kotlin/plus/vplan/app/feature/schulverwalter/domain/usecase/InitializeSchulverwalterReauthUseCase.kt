package plus.vplan.app.feature.schulverwalter.domain.usecase

import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.core.model.VppId

class InitializeSchulverwalterReauthUseCase(
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(vppId: VppId.Active): String? {
        return try {
            vppIdRepository.getSchulverwalterReauthUrl(vppId)
        } catch (e: NetworkException) {
            null
        }
    }
}
