package plus.vplan.app.feature.schulverwalter.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.besteschule.BesteSchuleRepository
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.VppId

class UpdateSchulverwalterAccessUseCase(
    private val besteSchuleRepository: BesteSchuleRepository,
    private val vppIdRepository: VppIdRepository,
) {
    suspend operator fun invoke(vppIdId: Int, accessToken: String) {
        val vppId = (vppIdRepository.getById(vppIdId).first() as? VppId.Active) ?: return
        if (vppId.schulverwalterConnection == null) return
        besteSchuleRepository.saveBesteSchuleAccess(
            userId = vppId.schulverwalterConnection!!.userId,
            vppId = vppId,
            token = accessToken
        )
    }
}