package plus.vplan.app.feature.schulverwalter.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.besteschule.BesteSchuleRepository
import plus.vplan.app.core.model.VppId
import plus.vplan.app.domain.repository.VppIdRepository

class UpdateSchulverwalterAccessUseCase(
    private val besteSchuleRepository: BesteSchuleRepository,
    private val vppIdRepository: VppIdRepository,
) {
    suspend operator fun invoke(vppIdId: Int, accessToken: String) {
        val vppId = (vppIdRepository.getByLocalId(vppIdId).first() as? VppId.Active) ?: return
        if (vppId.schulverwalterConnection == null) return
        besteSchuleRepository.saveBesteSchuleAccess(
            userId = vppId.schulverwalterConnection!!.userId,
            vppId = vppId,
            token = accessToken
        )
    }
}