package plus.vplan.app.feature.profile.page.domain.usecase

import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class HasVppIdLinkedUseCase(
    private val vppIdRepository: VppIdRepository
) {
    operator fun invoke() = vppIdRepository.getVppIds().map {
        it.any { vppId -> vppId is VppId.Active }
    }
}