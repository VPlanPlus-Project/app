package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.VppId

class HasVppIdLinkedUseCase(
    private val vppIdRepository: VppIdRepository
) {
    operator fun invoke() = vppIdRepository.getVppIds().map {
        it.any { vppId -> vppId is VppId.Active }
    }
}